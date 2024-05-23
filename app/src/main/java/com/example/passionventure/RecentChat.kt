package com.example.passionventure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class RecentChat : AppCompatActivity() {

    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_chat)

        searchButton = findViewById(R.id.main_search_btn)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            onBackPressed()
        }

        searchButton.setOnClickListener {
            startActivity(Intent(this@RecentChat, SearchUserActivity::class.java))
        }

    }
}