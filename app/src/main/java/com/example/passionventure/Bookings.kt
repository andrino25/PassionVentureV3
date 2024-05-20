package com.example.passionventure

import Booking
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toolbar
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
    private lateinit var TitleTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val currentUser = intent.getStringExtra("name").toString()
        noBookingsTextView = findViewById(R.id.noBookingsTextView)
        TitleTextView = findViewById(R.id.TitleTextView)
        // Set up RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        bookingsAdapter = BookingsAdapter(this)
        recyclerView.adapter = bookingsAdapter

        // Fetch bookings data from Firebase
        fetchBookingsFromDatabase(currentUser)
    }

    private fun fetchBookingsFromDatabase(currentUser: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings")

        // Query bookings where the 'userName' field matches the current user
        databaseReference.orderByChild("currUser").equalTo(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val bookingsList = mutableListOf<Booking>()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        booking?.let { bookingsList.add(it) }
                    }

                    // Check if the bookings list is empty
                    if (bookingsList.isEmpty()) {
                        // Hide RecyclerView and show "No bookings" message
                        recyclerView.visibility = View.GONE
                        noBookingsTextView.visibility = View.VISIBLE
                        TitleTextView.visibility = View.GONE // Hide the title
                    } else {
                        // Show RecyclerView and hide "No bookings" message
                        recyclerView.visibility = View.VISIBLE
                        noBookingsTextView.visibility = View.GONE
                        TitleTextView.visibility = View.VISIBLE // Show the title
                        // Submit the list to the adapter
                        bookingsAdapter.submitList(bookingsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }


}