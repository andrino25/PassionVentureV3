package com.example.passionventure

import User
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MentorAdapter(private val context: Context, private var mentorList: List<User>) :
    RecyclerView.Adapter<MentorAdapter.MentorViewHolder>() {

    private var filteredList: List<User> = mentorList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mentors_list, parent, false)
        return MentorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        val mentor = filteredList[position]
        holder.bind(mentor)
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class MentorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageButton)
        private val nameTextView: TextView = itemView.findViewById(R.id.name)
        private val categoryTextView: TextView = itemView.findViewById(R.id.category)

        fun bind(mentor: User) {
            //imageView.setImageResource(mentor.profileImageUrl)
            nameTextView.text = mentor.name
            categoryTextView.text = mentor.role
        }
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            mentorList
        } else {
            val lowercaseQuery = query.toLowerCase(Locale.getDefault())
            mentorList.filter { mentor ->
                mentor.name.toLowerCase(Locale.getDefault()).contains(lowercaseQuery) ||
                        mentor.role.toLowerCase(Locale.getDefault()).contains(lowercaseQuery)
            }
        }
        notifyDataSetChanged()
    }
}