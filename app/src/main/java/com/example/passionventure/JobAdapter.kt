package com.example.passionventure

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JobListAdapter(private var jobList: List<JobItem>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<JobListAdapter.JobViewHolder>(), Filterable {

    private var filteredJobList = jobList

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val jobDesc: TextView = itemView.findViewById(R.id.jobDesc)
        val jobCompany: TextView = itemView.findViewById(R.id.jobCompany)
        val jobCategory: TextView = itemView.findViewById(R.id.jobCategory)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val selectedItem = filteredJobList[position]
                listener.onItemClick(selectedItem)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: JobItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.job_list, parent, false)
        return JobViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val currentItem = filteredJobList[position]
        holder.jobDesc.text = currentItem.jobDesc
        holder.jobCompany.text = currentItem.jobCompany
        holder.jobCategory.text = currentItem.jobCategory
    }

    override fun getItemCount() = filteredJobList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = mutableListOf<JobItem>()

                if (constraint.isNullOrEmpty()) {
                    filteredResults.addAll(jobList)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()
                    for (item in jobList) {
                        if (item.jobDesc.toLowerCase().contains(filterPattern) ||
                            item.jobCompany.toLowerCase().contains(filterPattern) ||
                            item.jobCategory.toLowerCase().contains(filterPattern)) {
                            filteredResults.add(item)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredResults
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredJobList = results?.values as List<JobItem>
                notifyDataSetChanged()
            }
        }
    }

    fun filterList(filteredList: List<JobItem>) {
        filteredJobList = filteredList
        notifyDataSetChanged()
    }
}
