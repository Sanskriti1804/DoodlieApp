package com.example.drawingapplication

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class ImageDisplayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pages_screen)

        // Get the type of images to load from the Intent
        val imageType = intent.getStringExtra("IMAGE_TYPE")

        val themePageContainer: LinearLayout = findViewById(R.id.theme_page_container)
        val imageLoader = themeImagesLoader(this, themePageContainer)

        when (imageType) {
            "Fruits" -> imageLoader.loadFruitsImages() // Implement loadFruitsImages in themeImagesLoader
            "Animals" -> imageLoader.loadAnimalsImages() // Implement loadAnimalsImages in themeImagesLoader
            "Cartoons" -> imageLoader.loadCartoonsImages() // Implement loadCartoonsImages in themeImagesLoader
        }
    }
}