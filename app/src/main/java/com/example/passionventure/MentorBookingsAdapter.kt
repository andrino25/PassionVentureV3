package com.example.passionventure

import Booking
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class MentorBookingsAdapter(
    private val context: Context,
    private var bookingsList: List<Booking>,
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

        fun bind(booking: Booking) {
            userNameTextView.text = booking.currUser
            Picasso.get().load(booking.currUserImageUrl).into(imageHolder)

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

    fun submitList(list: List<Booking>) {
        bookingsList = list
        notifyDataSetChanged()
    }
}
