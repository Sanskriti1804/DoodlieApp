package com.example.drawingapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

//extends view class
//primary constructor - 2 para  //view - calls the constuctor
class DrawingView (context : Context, attrs : AttributeSet): View(context, attrs){

    private var mDrawPath : CustomPath? = null     //holds the custom path being drawn
    private var mCanvasBitmap : Bitmap? = null     //bitmap - drawing area
    //paint - style and color info abt drawing (geometrics, text and bitmap)
    private var mDrawPaint : Paint? = null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK     //sets the current drawing color
    private var canvas : Canvas? = null     //drawing surface where all the commmands are executed
    //immutable array list - keeps track of all the paths drawn so far
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    fun onClickUndo(){
        if(mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size-1))      //can also add removeAtLast
            invalidate()        //redraws the canvas
        }
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()        //instance of Paint class
        mDrawPath = CustomPath(color, mBrushSize)  //initializing cusrtomPath w curent color and brush size
        mDrawPaint!!.color = color      //!! - not  null
        mDrawPaint!!.style = Paint.Style.STROKE     //STROKE - outline not filling in
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND      //ROUND - 2 segements join will be round
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND    //cap - ends of strokes will be round
        mCanvasPaint = Paint(Paint.DITHER_FLAG) // sth related to animation
        //mBrushSize = 20.toFloat()     //default brush size
    }

    //method called if the size of the view changes -  layout or screen orientation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw , oldh)
        //create a new bitmpap obj w new width and height of the view
        //ARGB.. - each pixel is stored on 4 bytes(includes alppha and RGB channels)

        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        //creates a new canvas obj using the bitmap created
        canvas = Canvas(mCanvasBitmap!!)

    }

    //canvas(wall) here is  being used as a rendering tool for rendering the bitmap on the screen
    //hsndles custom drawing operation on the view
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //0f - cordinates usimg the paint specified
        mCanvasBitmap?.let {
            canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

            for (path in mPaths){       //iterates over each customPath obj in mPath list
                mDrawPaint!!.strokeWidth = path.brushThickness
                mDrawPaint!!.color = path.color
                canvas.drawPath(path, mDrawPaint!!)     //drawing the path
            }
        }

        //if there is a path to draw
        if(!mDrawPath!!.isEmpty){
            //setting the stroke width of the paint to the thickness specified in the path
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            //setting he color of the path
            mDrawPaint!!.color = mDrawPath!!.color
            //draw the path onto he canvas
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //retrive the x and y cordinate of touch point
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            //first touches the screen
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()     //reset the path to start the new drawing
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX, touchY)      //moves the path to the touch point
                    }
                }
            }

            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        //draws a contionous line from initial to end point
                        mDrawPath!!.lineTo(touchX, touchY)
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                //creates a new customPath obj
                //mDrawPath = CustomPath(color, mBrushSize)       //reset the path for the next drawing

                if (touchX != null && touchY != null) {
                    mPaths.add(mDrawPath!!)     //adding current  path to the list
                    mDrawPath = CustomPath(color, mBrushSize) // RESET PATH FOR NEXT DRAWING
                }
            }

            else -> return false        // ignore other actions

        }
        invalidate()        // req to redraw the view - ensures any changes made to the path are updated on the screen

        return true     // indicate that the event has been handled

    }

    fun setSizeForBrush(newSize : Float){
        //converts new size from dp to pixels
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,       //specifies the i/p val is in dp
            newSize,        //the i/p value to be converted
            resources.displayMetrics        //provides details about screen size and density
        )
        mDrawPaint!!.strokeWidth = mBrushSize       //updates the stroke width
    }

    fun setColor(newColor: String){     //string - hex codes
        color = Color.parseColor(newColor)      //converts the color string to int val
        mDrawPaint!!.color = color      //sets the color of the paint obj
    }


    //CUSTOM PATH - obj represents  a path that the user has drawn on the canvas
    //internal - only accessible inside the drawing view
    //inner - inner class can access the members of the outer class
    //path class - or creating and rendering complex shapes and paths
    internal inner class CustomPath(var color : Int, var brushThickness : Float) : Path(){
    }


}