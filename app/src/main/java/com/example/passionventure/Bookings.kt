package com.example.passionventure

import Booking
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
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
    private val originalBookingsList = mutableListOf<Pair<Booking, String>>() // To keep the original list
    private var currentFilter: String = "All" // To keep track of the current filter

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
        }, { booking, key ->
            startPaymentsActivity(booking)
        })
        recyclerView.adapter = bookingsAdapter

        // Set up filter button
        val filterButton = findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterMenu(it)
        }

        // Fetch bookings data from Firebase
        fetchBookingsFromDatabase(currentUser)
    }

    private fun fetchBookingsFromDatabase(currentUser: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("bookings")

        // Query bookings where the 'currUser' field matches the current user
        databaseReference.orderByChild("currUser").equalTo(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    originalBookingsList.clear() // Clear the original list before adding new items
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        booking?.let { originalBookingsList.add(Pair(it, bookingSnapshot.key!!)) }
                    }

                    // Reverse the list to show latest bookings first
                    originalBookingsList.reverse()

                    filterBookings(currentFilter) // Apply the current filter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }

    private fun startPaymentsActivity(booking: Booking) {
        val intent = Intent(this, Payments::class.java)
        intent.putExtra("bookingID", booking.bookingID)
        intent.putExtra("mentorName", booking.mentorName)
        intent.putExtra("currUser", booking.currUser)
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
        databaseReference.child(key).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Manual iteration and removal
                val iterator = originalBookingsList.iterator()
                while (iterator.hasNext()) {
                    val pair = iterator.next()
                    if (pair.second == key) {
                        iterator.remove()
                        break
                    }
                }
                filterBookings(currentFilter) // Refresh the adapter with the current filter
            } else {
                Toast.makeText(this, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showFilterMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.filter_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            currentFilter = item.title.toString()
            filterBookings(currentFilter)
            true
        }
        popup.show()
    }

    private fun filterBookings(status: String) {
        val filteredList = if (status == "All") {
            originalBookingsList.toList()
        } else {
            originalBookingsList.filter { it.first.bookingStatus == status }
        }

        bookingsAdapter.submitList(filteredList)
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            noBookingsTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noBookingsTextView.visibility = View.GONE
        }
    }
}
