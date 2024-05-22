package com.example.passionventure

import Resume
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResumeAdapter(private val context: Context, private var resumeList: List<Resume>) :
    RecyclerView.Adapter<ResumeAdapter.ResumeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResumeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.resume_list, parent, false)
        return ResumeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ResumeViewHolder, position: Int) {
        val currentItem = resumeList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = resumeList.size

    inner class ResumeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val resumeName: TextView = itemView.findViewById(R.id.userName)

        fun bind(resume: Resume) {
            resumeName.text = resume.username
            itemView.setOnClickListener {
                // Handle resume click by opening the document URL in a web browser or web view
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resume.resumeUrl))
                context.startActivity(intent)
            }
        }
    }

}
