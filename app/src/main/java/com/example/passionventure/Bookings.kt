package com.example.passionventure

import Booking
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookings)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val currentUser = intent.getStringExtra("name").toString()

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
                    bookingsAdapter.submitList(bookingsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }

}