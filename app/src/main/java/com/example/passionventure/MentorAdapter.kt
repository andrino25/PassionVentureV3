package com.example.passionventure

import User
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ui.mentor.MentorsFragment
import com.squareup.picasso.Picasso
import java.util.Locale

class MentorAdapter(private val context: MentorsFragment, private var mentorList: List<User>, private val currUser: String?, private val currUserProfile: String?) :
    RecyclerView.Adapter<MentorAdapter.MentorViewHolder>() {

    private var filteredList: List<User> = mentorList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mentors_list, parent, false)
        return MentorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        val mentor = filteredList[position]
        holder.bind(mentor)
        val gradient = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.BLACK, Color.TRANSPARENT))
        gradient.cornerRadius = 16f
        gradient.gradientType = GradientDrawable.LINEAR_GRADIENT
        gradient.setBounds(0, 0, holder.imageView.width, holder.imageView.height)
        holder.imageView.foreground = gradient
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class MentorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageButton)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val categoryTextView: TextView = itemView.findViewById(R.id.category)

        fun bind(mentor: User) {
            Picasso.get().load(mentor.profileImageUrl).into(imageView)
            nameTextView.text = mentor.name
            categoryTextView.text = mentor.profession

            itemView.setOnClickListener {
                val intent = Intent(context.requireContext(), MentorDetails::class.java).apply {
                    putExtra("name", mentor.name)
                    putExtra("profession", mentor.profession)
                    putExtra("description", mentor.description)
                    putExtra("imageUrl", mentor.profileImageUrl)
                    putExtra("currUser", currUser)
                    putExtra("currUserProfile", currUserProfile)
                }
                context.startActivity(intent)
            }
        }
    }

    fun filter(text: String) {
        filteredList = if (text.isEmpty()) {
            mentorList
        } else {
            mentorList.filter { mentor ->
                mentor.name.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault())) ||
                        mentor.profession.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))
            }
        }
        notifyDataSetChanged()
    }
}
