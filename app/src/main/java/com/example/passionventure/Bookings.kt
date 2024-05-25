package com.example.passionventure

import Booking
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Bookings : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsAdapter
    private lateinit var noBookingsTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        currentUser = intent.getStringExtra("name").toString()
        noBookingsTextView = findViewById(R.id.noBookingsTextView)
        titleTextView = findViewById(R.id.TitleTextView)

        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingsAdapter = BookingsAdapter(this, { booking, key ->
            showCancelDialog(booking, key)
        }, { mentorName ->
            startPaymentsActivity(mentorName)
        }, currentUser)
        recyclerView.adapter = bookingsAdapter

        // Fetch bookings data from Firebase
        fetchBookingsFromDatabase(currentUser)
    }

    private fun fetchBookingsFromDatabase(currentUser: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings")

        // Query bookings where the 'currUser' field matches the current user
        databaseReference.orderByChild("currUser").equalTo(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookingsList = mutableListOf<Pair<Booking, String>>()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        booking?.let { bookingsList.add(Pair(it, bookingSnapshot.key!!)) }
                    }

                    // Check if the bookings list is empty
                    if (bookingsList.isEmpty()) {
                        // Hide RecyclerView and show "No bookings" message
                        recyclerView.visibility = View.GONE
                        noBookingsTextView.visibility = View.VISIBLE
                    } else {
                        // Show RecyclerView and hide "No bookings" message
                        recyclerView.visibility = View.VISIBLE
                        noBookingsTextView.visibility = View.GONE
                        // Submit the list to the adapter
                        bookingsAdapter.submitList(bookingsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }
    private fun startPaymentsActivity(mentorName: String) {
        val intent = Intent(this, Payments::class.java)
        intent.putExtra("mentorName", mentorName)
        intent.putExtra("currUser", currentUser)
        startActivity(intent)
    }

    private fun showCancelDialog(booking: Booking, key: String) {
        AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Do you want to cancel this booking?")
            .setPositiveButton("Yes") { dialog, _ ->
                cancelBooking(key)
                dialog.dismiss()
                Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun cancelBooking(key: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings")
        databaseReference.child(key).removeValue()
    }
}

