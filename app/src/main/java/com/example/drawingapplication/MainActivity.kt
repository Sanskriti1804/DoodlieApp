package com.example.drawingapplication

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.text.CaseMap.Title
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Message
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.get
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    //storing img path in this
    lateinit var imageUri : Uri

    private val imageResultLauncher : ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()){
            isGranted ->
            Log.d("MainActivity", "Gallery permission granted")
            if (isGranted){
                Toast.makeText(this, "Permission granted for gallery access", Toast.LENGTH_LONG).show()

                val pickIntent = Intent(
                    Intent.ACTION_PICK,    //allows the user to pick a piece of data from a list of avsilable data source
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)   //tells the intent - picking an img from the device ext storage(gallery)
                openGalleryLauncher.launch(pickIntent)
            }
            else{
                Log.d("MainActivity", "Gallery permission denied")
                Toast.makeText(this, "Permission denied for gallery access", Toast.LENGTH_LONG).show()
            }
        }

    private var drawingView : DrawingView? = null
    private var mImageButtonCurrentPaint : ImageButton? = null
    var customProgressialog : Dialog? = null

    val openGalleryLauncher : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK && result.data != null){
                val imageBackGround : ImageView = findViewById(R.id.iv_app_background)

                imageBackGround.setImageURI(result.data?.data)
            }
        }

    private lateinit var navController: NavController

    private lateinit var canvasBtn : ImageButton
    private lateinit var themeBtn : ImageButton

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView= findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())
        Log.d("MainActivity", "DrawingView initialized")

        val ib_brush_btn : ImageButton = findViewById(R.id.ib_brush_btn)
        ib_brush_btn.setOnClickListener{
            showBrushSizeChooserDialog()
            Log.d("MainActivity", "Brush button clicked")
        }

        val ib_img_selector_btn : ImageButton = findViewById(R.id.ib_image_selector_btn)
        ib_img_selector_btn.setOnClickListener{
            //checks if the androud version is marshmallow or higher bc runtime perm was introduced in marshmallow
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)){
                Log.d("MainActivity", "Showing permission rationale dialog")
                showRationaleDialog("Permission Required", "Enable media image permission to select image")
            }
            else{
                Log.d("MainActivity", "Requesting gallery permission")
                imageResultLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }

        val ib_undo : ImageButton = findViewById(R.id.ib_undo_btn)
        ib_undo.setOnClickListener{
            Log.d("MainActivity", "Undo button clicked")
            drawingView?.onClickUndo()
        }

        imageUri = createImageUri()
        Log.d("MainActivity", "Initial Image URI: ${imageUri.toString()}")

        val ibSave : ImageButton = findViewById(R.id.ib_save_btn)
        ibSave.setOnClickListener{
            Log.d("MainActivity", "Save button clicked")
            showProgressDialog()
            //val bitmap = getBitmapFromView(container)
            val bitmap = getBitmapFromView(drawingView ?: return@setOnClickListener)
            storeBitmap(bitmap )

        }

        val ibPalette : ImageButton = findViewById(R.id.ib_pallete_btn)
        ibPalette.setOnClickListener{
            showColorPalletePopUp(it)
        }

        val ibDelete : ImageButton = findViewById(R.id.ib_delete_btn)
        ibDelete.setOnClickListener{
            recreate()
        }

        //checks if an image was passed and set it as bg
        val resId = intent.getIntExtra("IMAGE_RES_ID", -1)
        if (resId != -1){
            val imageBackGround : ImageView = findViewById(R.id.iv_app_background)
            imageBackGround.setImageResource(resId)
        }


    }

    private fun storeBitmap(bitmap: Bitmap) {
        /**val outputStream = applicationContext.contentResolver.openOutputStream(
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream!!.close())**/
        Log.d("MainActivity", "Storing bitmap")
        val contentResolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "drawing_app_image.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingApp")
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        Log.d("MainActivity", "Created Image URI: ${uri.toString()}") // Add this line
        //return uri
        uri?.let {
            cancelProgressDialog()
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                outputStream.close()
                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
                shareImage(it.toString())
            }
        }

    }

    private fun showBrushSizeChooserDialog(){
        Log.d("MainActivity", "Showing brush size chooser dialog")
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size:")
        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)

        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }

        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }

        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    //display a pop up for color pallete
    private fun showColorPalletePopUp(anchor : View){
        //retrives layout inflater service for converting xml layout in view object
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //creates a view, null - not attatched to any parent view
        val popupView = inflater.inflate(R.layout.color_pallete_pop_up, null)

        //create a popup window
        val popupWindow = PopupWindow(      //instance of popup window
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,     //height and width of popup
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true    //the popup will be focused and interact with user input
                             //allowing it to handle input events and be dismissed properly
        )

        // Measure the dimensions of the popup view
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        // Calculate the x and y offsets to display the popup above the button
        val xOffset = (anchor.width - popupWidth) / 2
        val yOffset = -popupHeight - 140// Show above the button

        // Show the popup window at the position of the anchor view
        popupWindow.showAsDropDown(anchor, xOffset, yOffset)
    }

    fun paintCLicked(view : View){      // view was passed as a parameter for on click event handling
        Log.d("MainActivity", "Paint button clicked")
        if(view !== mImageButtonCurrentPaint){      // only if the currently selected view is different than already selected button
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            Log.d("MainActivity", "Setting color to $colorTag")
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_pallete_pressed)
            )

            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_pallete_normal)
            )

            mImageButtonCurrentPaint = view     //???
        }
    }

    private fun showRationaleDialog(
        title: String,
        message: String,
    ){
        Log.d("MainActivity", "Showing rationale dialog")
        //creates an instance for alertdialog.buildr        // this - currnent context/activity
        val builder : AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("cancel"){
                dialog, _-> dialog.dismiss()        //_-> lambda fnn to dismiss the dialog
            }
        builder.create().show()
    }

    //converting view to bitmap - to save share or upload
    private  fun getBitmapFromView(view: View) : Bitmap{
        Log.d("MainActivity", "Converting view to bitmap")
        //creates a bitmap w same width and height as view
        val returnedBitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap) //canvas to draw on the bitmap
        val imageBackGround: ImageView = findViewById(R.id.iv_app_background)
        //val bgDrawable = view.background
        val bgDrawable = imageBackGround.drawable
        /*if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }*/
        bgDrawable?.let {
            it.setBounds(0, 0, canvas.width, canvas.height)
            it.draw(canvas)
        } ?: canvas.drawColor(Color.WHITE)

        view.draw(canvas)   //draws the entire views on the canvas
        return returnedBitmap
    }

    private fun createImageUri() : Uri{
        Log.d("MainActivity", "Creating image URI")
        //created a file at path 'applicationContext.filesDir' w name 'drawing_app_photos'
        val image = File(applicationContext.filesDir, "drawing_app_photos")

        //getting uri of the file to access the file
        //return FileProvider.getUriForFile
        val uri = FileProvider.getUriForFile (
            this, //can also use this
            "com.example.drawingapplication.fileprovider",
            image
        )
        Log.d("MainActivity", "Created Image URI: ${uri.toString()}") // Add this line
        return uri
    }

    private fun showProgressDialog(){
        Log.d("MainActivity", "Showing progress dialog")
        customProgressialog = Dialog(this@MainActivity)

        customProgressialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressialog?.show()
    }

    private fun cancelProgressDialog(){
        Log.d("MainActivity", "Cancelling progress dialog")
        if(customProgressialog != null){
            customProgressialog?.dismiss()
            customProgressialog = null
        }
    }

    private fun shareImage(result : String){
        Log.d("MainActivity", "Sharing image: $result")
        MediaScannerConnection.scanFile(this, arrayOf(result), null){
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

