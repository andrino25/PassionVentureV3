package com.example.passionventure

import Booking
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class BookingsAdapter(private val context: Context) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {
    private var bookingsList: List<Booking> = listOf()

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mentorNameTextView: TextView = itemView.findViewById(R.id.mentorName)
        private val mentorProfessionTextView: TextView = itemView.findViewById(R.id.mentorProfession)
        private val imageHolder: ImageView = itemView.findViewById(R.id.imageHolder)
        private val bookingStatusTextView: TextView = itemView.findViewById(R.id.bookingStatus)

        fun bind(booking: Booking) {
            mentorNameTextView.text = booking.mentorName
            mentorProfessionTextView.text = booking.mentorProfession
            bookingStatusTextView.text = booking.bookingStatus

            // Set text color based on booking status
            val statusColor = when (booking.bookingStatus) {
                "Pending" -> R.color.orange
                "Accepted" -> R.color.green   // Use your color resource for green
                "Rejected" -> R.color.red     // Use your color resource for red
                else -> android.R.color.black // Default color (black)
            }
            bookingStatusTextView.setTextColor(itemView.context.getColor(statusColor))

            Picasso.get().load(booking.mentorImageUrl).into(imageHolder)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_list, parent, false)
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
