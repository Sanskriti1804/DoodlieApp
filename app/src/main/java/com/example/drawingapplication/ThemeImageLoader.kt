package com.example.drawingapplication

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout

class themeImagesLoader(private val context: Context, private val imageContainer : LinearLayout){

    private val fruitsImages = intArrayOf(
        R.drawable.fruit_1,
        R.drawable.fruit_2,
        R.drawable.fruit_3,
        R.drawable.fruit_4,
        R.drawable.fruit_5,
        R.drawable.fruit_6
    )

    private val animalsImages = intArrayOf(
        R.drawable.animal_1,
        R.drawable.animal_2,
        R.drawable.animal_3,
        R.drawable.animal_4,
        R.drawable.animal_5,
        R.drawable.animal_6
    )

    private val cartoonsImages = intArrayOf(
        R.drawable.fruit_1,
        R.drawable.fruit_2,
        R.drawable.fruit_3,
        R.drawable.fruit_4,
        R.drawable.fruit_5,
        R.drawable.fruit_6
    )

    fun loadFruitsImages() {
        loadImages(fruitsImages)
    }

    fun loadAnimalsImages() {
        loadImages(animalsImages)
    }

    fun loadCartoonsImages() {
        loadImages(cartoonsImages)
    }

    //looping through the images to display them in linear layout
    fun loadImages(imageResource : IntArray)
    {
        for(resId in imageResource.indices){
            //Log.d("ThemeImageLoader", "Loading resource ID: $resId")
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                setPadding(0,0,0,32)
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(imageResource[resId])
                setOnClickListener {
                    openCanvas(imageResource[resId])
                }
            }
            imageContainer.addView(imageView)
        }
    }

    private fun openCanvas(resId : Int){
        val intent = Intent(context, MainActivity::class.java).apply{
            putExtra("IMAGE_RES_ID", resId)
        }
        context.startActivity(intent)
    }
}