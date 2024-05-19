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
import com.example.passionventure.databinding.ActivityCompanyHomePageBinding

class CompanyHomePage : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_company_home_page)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        // Retrieve username from intent
        val username = intent.getStringExtra("username")

        // Navigate to HomeFragment and pass username
        val bundle = Bundle()
        bundle.putString("username", username)
        navController.navigate(R.id.navigation_home, bundle)

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Navigate to HomeFragment and pass username
                    val username = intent.getStringExtra("username")
                    val bundle = Bundle()
                    bundle.putString("username", username)
                    navController.navigate(R.id.navigation_home, bundle)
                    true
                }

                R.id.navigation_dashboard -> {
                    // Navigate to DashboardFragment and pass username
                    val username = intent.getStringExtra("username")
                    val bundle = Bundle()
                    bundle.putString("username", username)
                    navController.navigate(R.id.navigation_dashboard, bundle)
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
        val menuItem1 = popupMenu.menu.findItem(R.id.messages)
        val menuItem2 = popupMenu.menu.findItem(R.id.Bookings)
        // Hide them
        menuItem1.isVisible = false
        menuItem2.isVisible = false

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val currUser = intent.getStringExtra("username")
                    val intent = Intent(this, ProfileView::class.java)
                    intent.putExtra("username", currUser)
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
