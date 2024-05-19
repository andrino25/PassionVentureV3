package com.example.passionventure

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.passionventure.databinding.ActivityEnthusiastHomePageBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class EnthusiastHomePage : AppCompatActivity() {

    private lateinit var binding: ActivityEnthusiastHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEnthusiastHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_enthusiast_home_page)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_mentors, R.id.navigation_macthing, R.id.navigation_resources
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

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
                R.id.Bookings -> {
                    val currUser = intent.getStringExtra("name")
                    val intent = Intent(this, Bookings::class.java)
                    intent.putExtra("name", currUser)
                    startActivity(intent)
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
