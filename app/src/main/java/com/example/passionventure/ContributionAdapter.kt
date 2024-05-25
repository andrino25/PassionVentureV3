package com.example.passionventure

import Contribution
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ContributionAdapter(
    private val context: Context,
    private val onItemClick: (Contribution) -> Unit
) : ListAdapter<Contribution, ContributionAdapter.ContributionViewHolder>(ContributionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contribution_list, parent, false)
        return ContributionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContributionViewHolder, position: Int) {
        val contribution = getItem(position)
        holder.bind(contribution)
    }

    inner class ContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageHolder)
        private val titleTextView: TextView = itemView.findViewById(R.id.title)
        private val mentorNameTextView: TextView = itemView.findViewById(R.id.mentorName)
        private val datePublishedTextView: TextView = itemView.findViewById(R.id.datePublished)

        init {
            // Set long click listener to show the popup menu
            itemView.setOnLongClickListener { view ->
                showPopupMenu(view, getItem(adapterPosition))
                true
            }

            // Set click listener for item
            itemView.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        private fun showPopupMenu(view: View, contribution: Contribution) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.job_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.update -> {
                        // Handle update contribution
                        val intent = Intent(context, UpdateContribution::class.java).apply {
                            putExtra("id", contribution.id)
                            putExtra("contributionTitle", contribution.title)
                            putExtra("contributionDesc", contribution.content)
                        }
                        context.startActivity(intent)
                        true
                    }
                    R.id.delete -> {
                        // Handle delete contribution
                        deleteContribution(contribution.id)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun deleteContribution(contributionId: String) {
            val contributionsReference = FirebaseDatabase.getInstance().getReference("contributions")
            contributionsReference.child(contributionId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Deleted contribution successfully", Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Failed to delete contribution", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun bind(contribution: Contribution) {
            Picasso.get().load(contribution.imageUrl).into(imageView)
            titleTextView.text = contribution.title
            mentorNameTextView.text = "By: ${contribution.mentorName}"
            datePublishedTextView.text = contribution.datePublished
        }
    }

    class ContributionDiffCallback : DiffUtil.ItemCallback<Contribution>() {
        override fun areItemsTheSame(oldItem: Contribution, newItem: Contribution): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contribution, newItem: Contribution): Boolean {
            return oldItem == newItem
        }
    }
}
