package com.example.passionventure

import Booking
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso

class MentorDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val name = intent.getStringExtra("name")
        val profession = intent.getStringExtra("profession")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")

        val nameTextView: TextView = findViewById(R.id.mentor_name)
        val professionTextView: TextView = findViewById(R.id.mentor_profession)
        val descriptionTextView: TextView = findViewById(R.id.mentor_description)
        val imageView: ShapeableImageView = findViewById(R.id.mentor_image)
        val txt1: TextView = findViewById(R.id.titleDesc)

        txt1.text = "About $name"
        nameTextView.text = name
        professionTextView.text = profession
        descriptionTextView.text = description
        Picasso.get().load(imageUrl).into(imageView)

        val bookBtn = findViewById<Button>(R.id.bookBtn)
        bookBtn.setOnClickListener {
            // Get mentor details from intent extras
            val mentorName = intent.getStringExtra("name") ?: ""
            val mentorProfession = intent.getStringExtra("profession") ?: ""
            val mentorDescription = intent.getStringExtra("description") ?: ""
            val mentorImageUrl = intent.getStringExtra("imageUrl") ?: ""

            // Get current user's name from intent extras
            val currUser = intent.getStringExtra("currUser") ?: ""
            val currUserProfile = intent.getStringExtra("currUserProfile") ?: ""
            var status = "Pending"
            // Create a Booking instance
            val booking = Booking(currUser, mentorName, mentorProfession,mentorImageUrl, currUserProfile, status)

            // Save booking to Firebase Realtime Database
            saveBookingToDatabase(booking)
        }
    }

        private fun saveBookingToDatabase(booking: Booking) {
            val database = FirebaseDatabase.getInstance().reference.child("bookings")
            val newBookingRef = database.push()
            newBookingRef.setValue(booking)
                .addOnSuccessListener {
                    // Handle successful booking
                    Toast.makeText(this, "Booked successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(this, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }