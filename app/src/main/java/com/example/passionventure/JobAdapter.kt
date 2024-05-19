package com.example.passionventure

import Jobs
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class JobAdapter(private val context: Context, private var jobList: List<Jobs>) :
    RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

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
                val intent = Intent(context, JobDetails::class.java).apply {
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

            itemView.setOnLongClickListener { view ->
                showPopupMenu(view, job)
                true
            }
        }

        private fun showPopupMenu(view: View, job: Jobs) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.job_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.update -> {
                        // Handle update job
                        val intent = Intent(context, UpdateJobActivity::class.java).apply {
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
                        true
                    }
                    R.id.delete -> {
                        // Handle delete job
                        deleteJob(job.title)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun deleteJob(jobTitle: String) {
            val jobsReference = FirebaseDatabase.getInstance().getReference("jobs")
            jobsReference.orderByChild("title").equalTo(jobTitle).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            snapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Deleted job successfully", Toast.LENGTH_SHORT).show()
                                    jobList = jobList.filterNot { it.title == jobTitle }
                                    filteredJobList = filteredJobList.filterNot { it.title == jobTitle }
                                    notifyDataSetChanged()
                                } else {
                                    // Handle the error
                                    // Display a toast or log the error message
                                    // For example:
                                    Toast.makeText(context, "Failed to delete job", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        // Job with the given title not found
                        // Handle this case if needed
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    // For example:
                    Toast.makeText(context, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

    }

    fun filter(text: String) {
        filteredJobList = if (text.isEmpty()) {
            jobList
        } else {
            jobList.filter { job ->
                job.company.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault())) ||
                        job.category.toLowerCase(Locale.getDefault()).contains(text.toLowerCase(Locale.getDefault()))
            }
        }
        notifyDataSetChanged()
    }
}
