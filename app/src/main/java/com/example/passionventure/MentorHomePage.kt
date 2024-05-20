package com.example.passionventure

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.passionventure.databinding.ActivityMentorHomePageBinding

class MentorHomePage : AppCompatActivity() {

    private lateinit var binding: ActivityMentorHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMentorHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Find the Toolbar and set it as the ActionBar
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_mentor_home_page)

        // Passing each menu ID as a set of Ids because each menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_mentor_bookings
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Retrieve currUser from the intent
        val currUser = intent.getStringExtra("name")

        // Set up the BottomNavigationView item selection listener
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val username = intent.getStringExtra("username")
                    val bundle = Bundle().apply {
                        putString("username", username)
                    }
                    navController.navigate(R.id.navigation_home, bundle)
                    true
                }
                R.id.navigation_dashboard -> {
                    val username = intent.getStringExtra("username")
                    val bundle = Bundle().apply {
                        putString("username", username)
                    }
                    navController.navigate(R.id.navigation_dashboard, bundle)
                    true
                }
                R.id.navigation_mentor_bookings -> {
                    val bundle = Bundle().apply {
                        putString("username", currUser)
                    }
                    navController.navigate(R.id.navigation_mentor_bookings, bundle)
                    true
                }
                else -> false
            }
        }

        // Set up back button click listener
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Set up menu button click listener
        val menuButton: ImageButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

        // Find the items you want to hide
        val menuItem1 = popupMenu.menu.findItem(R.id.Bookings)
        // Hide them
        menuItem1.isVisible = false

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val currUser = intent.getStringExtra("username")
                    val intent = Intent(this, ProfileView::class.java)
                    intent.putExtra("username", currUser)
                    startActivity(intent)
                    true
                }
                R.id.messages -> {
                    // Handle Messages option click
                    true
                }
                R.id.logout -> {
                    val intent = Intent(this, SignInView::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
