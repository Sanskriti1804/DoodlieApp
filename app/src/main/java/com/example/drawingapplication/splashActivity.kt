package com.example.drawingapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)


    //navigate to mainscreen after 2 sec delay
    Handler(Looper.getMainLooper()).postDelayed( {
        val intent = Intent(this, HomeScreenActivity::class.java)
        startActivity(intent)
        finish()        //so that the user cant go back
    },2500)
    }

}