package com.example.passionventure

import Booking
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class MentorDetails : AppCompatActivity() {

    private var bookingId: String = "" // Initialize with an empty string

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
            val booking = Booking(bookingId, currUser, mentorName, mentorProfession, mentorImageUrl, currUserProfile, status)

            // Save booking to Firebase Realtime Database
            saveBookingToDatabase(booking)
        }
    }

    private fun saveBookingToDatabase(booking: Booking) {
        val database = FirebaseDatabase.getInstance().reference.child("bookings")

        // Save the new booking with generated booking ID
        val newBookingRef = database.push()
        bookingId = newBookingRef.key.toString()

        bookingId.let { id ->
            // Set the booking ID for the booking object
            val bookingWithId = booking.copy(bookingID = id)

            // Check if there are existing bookings with the same mentorName and status "Pending" or "Accepted"
            database.orderByChild("mentorName").equalTo(booking.mentorName)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var canBook = true
                        for (bookingSnapshot in dataSnapshot.children) {
                            val existingBooking = bookingSnapshot.getValue(Booking::class.java)
                            if (existingBooking != null && (existingBooking.bookingStatus == "Pending" || existingBooking.bookingStatus == "Accepted")) {
                                // Prevent booking if there's an existing booking with status "Pending" or "Accepted"
                                canBook = false
                                break
                            }
                        }
                        if (canBook) {
                            // Save the new booking with generated booking ID if there's no existing booking with status "Pending" or "Accepted"
                            newBookingRef.setValue(bookingWithId)
                                .addOnSuccessListener {
                                    // Handle successful booking
                                    Toast.makeText(this@MentorDetails, "Booked successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                    Toast.makeText(this@MentorDetails, "Booking failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Prevent booking if there's an existing booking with status "Pending" or "Accepted"
                            Toast.makeText(this@MentorDetails, "Cannot book again. Previous booking is pending or accepted.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                        Toast.makeText(this@MentorDetails, "Failed to check existing bookings", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
