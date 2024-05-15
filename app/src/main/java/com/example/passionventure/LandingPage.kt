package com.example.passionventure

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class LandingPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the app is being opened for the first time
        val sharedPreferences = getSharedPreferences("com.example.passionventure", Context.MODE_PRIVATE)
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // First time opening the app, show the landing page
            setContentView(R.layout.activity_landing_page)

            val toSignIn = findViewById<Button>(R.id.getStartedButton)
            toSignIn.setOnClickListener {
                // Redirect to SignInView
                val intent = Intent(this, SignInView::class.java)
                startActivity(intent)

                // Update the flag so next time the app is opened, it goes directly to SignInView
                sharedPreferences.edit().putBoolean("isFirstTime", false).apply()
            }
        } else {
            // Not the first time, redirect to SignInView directly
            val intent = Intent(this, SignInView::class.java)
            startActivity(intent)
            finish()
        }
    }
}
