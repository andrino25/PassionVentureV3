package com.example.passionventure

import Jobs
import Resume
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class JobAdapter(
    private val context: Context,
    private var jobList: List<Jobs>,
    private val resumeList: List<Resume>,
    private val onItemClick: (Jobs, Resume?) -> Unit, // Callback function
    private val isEditable: Boolean // Editable flag
) : ListAdapter<Jobs, JobAdapter.JobViewHolder>(JobDiffCallback()) {

    private var filteredList: List<Jobs> = jobList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.job_list, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val currentItem = filteredList[position]
        val currentResume = resumeList.find { it.jobTitle == currentItem.title && it.jobCompany == currentItem.company }
        holder.bind(currentItem, currentResume)
    }

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

            if (isEditable) {
                itemView.setOnLongClickListener { view ->
                    showPopupMenu(view, job)
                    true
                }
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
                        showDeleteConfirmationDialog(job)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun showDeleteConfirmationDialog(job: Jobs) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Job")
            builder.setMessage("Are you sure you want to delete this job?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteJob(job.title)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
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
                                    // Remove the job from the list and update the adapter
                                    val newList = currentList.toMutableList().apply {
                                        removeAll { it.title == jobTitle }
                                    }
                                    submitList(newList)
                                } else {
                                    Toast.makeText(context, "Failed to delete job", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Job not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    fun filter(text: String) {
        val searchText = text.toLowerCase(Locale.getDefault())
        filteredList = if (searchText.isEmpty()) {
            jobList
        } else {
            jobList.filter { job ->
                job.title.toLowerCase(Locale.getDefault()).contains(searchText) ||
                        job.company.toLowerCase(Locale.getDefault()).contains(searchText) ||
                        job.category.toLowerCase(Locale.getDefault()).contains(searchText)
            }
        }
        submitList(filteredList)
    }

    class JobDiffCallback : DiffUtil.ItemCallback<Jobs>() {
        override fun areItemsTheSame(oldItem: Jobs, newItem: Jobs): Boolean {
            return oldItem.title == newItem.title && oldItem.company == newItem.company
        }

        override fun areContentsTheSame(oldItem: Jobs, newItem: Jobs): Boolean {
            return oldItem == newItem
        }
    }
}
