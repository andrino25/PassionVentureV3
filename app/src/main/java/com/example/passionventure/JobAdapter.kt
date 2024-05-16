package com.example.passionventure

import Jobs
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ui.matching.MatchingFragment
import com.example.passionventure.ui.mentor.MentorsFragment
import java.util.Locale

class JobAdapter(private val context: MatchingFragment, private var jobList: List<Jobs>) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>(){

    private var filteredJobList: List<Jobs> = jobList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.job_list, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val currentItem = filteredJobList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = filteredJobList.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val jobDesc: TextView = itemView.findViewById(R.id.jobDesc)
        private val jobCompany: TextView = itemView.findViewById(R.id.jobCompany)
        private val jobCategory: TextView = itemView.findViewById(R.id.jobCategory)

        fun bind(job: Jobs) {
            jobDesc.text = job.title
            jobCompany.text = "✅ ${job.company}"
            jobCategory.text = "⚫ ${job.category}"

            itemView.setOnClickListener {
                val intent = Intent(context.requireContext(), JobDetails::class.java).apply {
                    putExtra("jobTitle", job.title)
                    putExtra("jobDesc", job.description)
                    putExtra("jobCompany", job.company)
                    putExtra("jobCategory", job.category)
                    putExtra("jobExperience", job.experience)
                    putExtra("jobAttainment", job.attainment)
                    putExtra("jobSalary", job.salary)
                    putExtra("jobAddress", job.companyAddress)
                    putExtra("companyDescription", job.companyDescription)
                }
                context.startActivity(intent)
            }
        }
    }


    fun filter(text: String) {
        filteredJobList = if (text.isEmpty()) {
            jobList
        } else {
            jobList.filter { job ->
                job.company.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault())) ||
                        job.category.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(
                            Locale.getDefault()))
            }
        }
        notifyDataSetChanged()
    }
}
