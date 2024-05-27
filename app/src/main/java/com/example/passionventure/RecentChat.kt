package com.example.passionventure

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecentChat : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var chatFragment: ChatFragment
    private lateinit var currUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_chat)

        currUser = intent.getStringExtra("username").toString()

        // Initialize views
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchButton = findViewById(R.id.main_search_btn)
        backButton = findViewById(R.id.backButton)

        // Initialize fragment with the username
        chatFragment = ChatFragment.newInstance(currUser)

        // Set click listener for the back button
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set click listener for the search button
        searchButton.setOnClickListener {
            startActivity(Intent(this@RecentChat, SearchUserActivity::class.java))
        }

        // Set the navigation item selected listener
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frame_layout, chatFragment) // Ensure fragment_container is the ID of your Fragment container in the layout XML
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Set the default selected item
        bottomNavigationView.selectedItemId = R.id.menu_chat
    }
}
