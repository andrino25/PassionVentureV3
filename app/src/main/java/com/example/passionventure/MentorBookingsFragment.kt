package com.example.passionventure

import Booking
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passionventure.databinding.FragmentMentorBookingsBinding
import com.google.firebase.database.*

class MentorBookingsFragment : Fragment() {

    private var _binding: FragmentMentorBookingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var bookingsAdapter: MentorBookingsAdapter
    private lateinit var bookingsList: MutableList<Booking>
    private lateinit var mentorName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mentorName = it.getString("username").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMentorBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference.child("bookings")
        bookingsList = mutableListOf()
        bookingsAdapter = MentorBookingsAdapter(requireContext(), bookingsList, object : MentorBookingsAdapter.OnItemClickListener {
            override fun onAcceptClick(booking: Booking) {
                updateBookingStatus(booking)
            }

            override fun onRejectClick(booking: Booking) {
                updateBookingStatus(booking)
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookingsAdapter
        }

        fetchBookings()
    }

    private fun fetchBookings() {
        database.orderByChild("mentorName").equalTo(mentorName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    bookingsList.clear()
                    for (bookingSnapshot in snapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        booking?.let {
                            bookingsList.add(it)
                        }
                    }
                    bookingsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to load bookings: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateBookingStatus(booking: Booking) {
        val bookingRef = database.orderByChild("mentorName").equalTo(mentorName)
        val newStatus = if (booking.bookingStatus == "Accepted") "Accepted" else "Rejected"
        bookingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val currentBooking = snapshot.getValue(Booking::class.java)
                        if (currentBooking != null && currentBooking.currUser == booking.currUser) {
                            snapshot.ref.child("bookingStatus").setValue(newStatus)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Booking $newStatus", Toast.LENGTH_SHORT).show()
                                    // Remove the item from the list
                                    val index = bookingsList.indexOf(booking)
                                    if (index != -1) {
                                        bookingsList.removeAt(index)
                                        // Update the adapter
                                        bookingsAdapter.notifyItemRemoved(index)
                                        // Check if the RecyclerView is empty after removing the item
                                        if (bookingsList.isEmpty()) {
                                            hideRecyclerView()
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to update status", Toast.LENGTH_SHORT).show()
                                }
                            break
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed to get job key: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hideRecyclerView() {
        binding.recyclerView.visibility = View.GONE
        binding.noBookingsTextView.visibility = View.VISIBLE
        binding.title.visibility = View.GONE // Assuming you have a TextView for showing no bookings message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
