package com.example.passionventure

import Booking
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MentorBookingsAdapter(
    private val context: Context,
    private var bookingsList: MutableList<Booking>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<MentorBookingsAdapter.BookingViewHolder>() {

    interface OnItemClickListener {
        fun onAcceptClick(booking: Booking)
        fun onRejectClick(booking: Booking)
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userNameTextView: TextView = itemView.findViewById(R.id.userName)
        private val imageHolder: ImageView = itemView.findViewById(R.id.imageHolder)
        private val acceptBtn: ImageButton = itemView.findViewById(R.id.acceptBtn)
        private val rejectBtn: ImageButton = itemView.findViewById(R.id.rejectBtn)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(booking: Booking) {
            userNameTextView.text = booking.currUser
            Picasso.get().load(booking.currUserImageUrl).into(imageHolder)

            // Update visibility based on booking status
            if (booking.bookingStatus == "Pending") {
                acceptBtn.visibility = View.VISIBLE
                rejectBtn.visibility = View.VISIBLE
                statusTextView.visibility = View.GONE
            } else {
                acceptBtn.visibility = View.GONE
                rejectBtn.visibility = View.GONE
                statusTextView.visibility = View.VISIBLE
                statusTextView.text = booking.bookingStatus

                val statusColor = when (booking.bookingStatus) {
                    "Accepted" -> R.color.green   // Use your color resource for green
                    "Rejected" -> R.color.red     // Use your color resource for red
                    else -> android.R.color.black // Default color (black)
                }
                statusTextView.setTextColor(ContextCompat.getColor(itemView.context, statusColor))
            }

            acceptBtn.setOnClickListener {
                listener.onAcceptClick(booking)
            }

            rejectBtn.setOnClickListener {
                listener.onRejectClick(booking)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mentor_bookings, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookingsList[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int {
        return bookingsList.size
    }

    fun submitList(list: MutableList<Booking>) {
        bookingsList = list
        notifyDataSetChanged()
    }

    fun addBookingToTop(booking: Booking) {
        bookingsList.add(0, booking)
        notifyItemInserted(0)
    }

    fun moveToBottom(booking: Booking) {
        val index = bookingsList.indexOf(booking)
        if (index != -1) {
            bookingsList.removeAt(index)
            bookingsList.add(booking)
            notifyItemMoved(index, bookingsList.size - 1)
        }
    }
}

