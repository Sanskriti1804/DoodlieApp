package com.example.drawingapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class ThemeActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        val fruitsFrame: FrameLayout = findViewById(R.id.fruits_frame)
        val animalsFrame: FrameLayout = findViewById(R.id.animals_frame)
        val cartoonsFrame: FrameLayout = findViewById(R.id.cartoons_frame)

        fruitsFrame.setOnClickListener {
            openImageDisplayActivity("Fruits")
        }
        animalsFrame.setOnClickListener {
            openImageDisplayActivity("Animals")
        }
        cartoonsFrame.setOnClickListener {
            openImageDisplayActivity("Cartoons")
        }
    }

        private fun openImageDisplayActivity(imageType: String) {
            val intent = Intent(this, ImageDisplayActivity::class.java).apply {
                putExtra("IMAGE_TYPE", imageType)
            }
            startActivity(intent)
        }
    }
