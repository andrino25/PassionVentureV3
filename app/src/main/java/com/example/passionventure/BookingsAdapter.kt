package com.example.passionventure

import Booking
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class BookingsAdapter(
    private val activity: Bookings,
    private val onCancelClick: (Booking, String) -> Unit
) : ListAdapter<Pair<Booking, String>, BookingsAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_list, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position).first, getItem(position).second)
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageHolder: com.google.android.material.imageview.ShapeableImageView = itemView.findViewById(R.id.imageHolder)
        private val mentorName: TextView = itemView.findViewById(R.id.mentorName)
        private val mentorProfession: TextView = itemView.findViewById(R.id.mentorProfession)
        private val bookingStatus: TextView = itemView.findViewById(R.id.bookingStatus)
        private val cancelButton: ImageButton = itemView.findViewById(R.id.cancelButton)

        fun bind(booking: Booking, key: String) {
            Picasso.get().load(booking.mentorImageUrl).into(imageHolder)
            mentorName.text = booking.mentorName
            mentorProfession.text = booking.mentorProfession
            bookingStatus.text = booking.bookingStatus

            val statusColor = when (booking.bookingStatus) {
                "Pending" -> R.color.orange
                "Accepted" -> R.color.green   // Use your color resource for green
                "Rejected" -> R.color.red     // Use your color resource for red
                else -> android.R.color.black // Default color (black)
            }
            bookingStatus.setTextColor(itemView.context.getColor(statusColor))

            if (booking.bookingStatus != "Pending" ) {
                cancelButton.visibility = View.GONE
            } else {
                cancelButton.visibility = View.VISIBLE
                cancelButton.setOnClickListener {
                    onCancelClick(booking, key)
                }
            }
        }
    }

    class BookingDiffCallback : DiffUtil.ItemCallback<Pair<Booking, String>>() {
        override fun areItemsTheSame(oldItem: Pair<Booking, String>, newItem: Pair<Booking, String>): Boolean {
            return oldItem.second == newItem.second
        }

        override fun areContentsTheSame(oldItem: Pair<Booking, String>, newItem: Pair<Booking, String>): Boolean {
            return oldItem.first == newItem.first
        }
    }
}
