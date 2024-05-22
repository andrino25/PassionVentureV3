// JobAdapter.kt
package com.example.passionventure

import Jobs
import Resume
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ui.matching.MatchingFragment
import java.util.Locale

class JobAdapter(
    private val context: Context,
    private var jobList: List<Jobs>,
    private var resumeList: List<Resume>,
    private val onItemClick: (Jobs, Resume?) -> Unit // Callback function
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.job_list, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val currentItem = jobList[position]
        val currentResume = resumeList.find { it.jobTitle == currentItem.title && it.jobCompany == currentItem.company }
        holder.bind(currentItem, currentResume)
    }

    override fun getItemCount() = jobList.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobDesc: TextView = itemView.findViewById(R.id.jobDesc)
        private val jobCompany: TextView = itemView.findViewById(R.id.jobCompany)
        private val jobCategory: TextView = itemView.findViewById(R.id.jobCategory)

        fun bind(job: Jobs, resume: Resume?) {
            jobDesc.text = job.title
            jobCompany.text = "✅ ${job.company}"
            jobCategory.text = "⚫ ${job.category}"

            itemView.setOnClickListener {
                onItemClick(job, resume)
            }
        }
    }
}
