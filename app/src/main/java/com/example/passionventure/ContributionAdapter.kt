package com.example.passionventure

import Contribution
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import java.util.Locale

class ContributionAdapter(
    private val context: Context,
    private var contributionList: List<Contribution>,
    private val onItemClick: (Contribution) -> Unit,
    private val isEditable: Boolean
) : ListAdapter<Contribution, ContributionAdapter.ContributionViewHolder>(ContributionDiffCallback()) {

    private var filteredList: List<Contribution> = contributionList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.contribution_list, parent, false)
        return ContributionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContributionViewHolder, position: Int) {
        val contribution = filteredList[position]
        holder.bind(contribution, context, onItemClick, isEditable)
    }

    inner class ContributionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.imageHolder)
        private val titleTextView: TextView = itemView.findViewById(R.id.title)
        private val mentorNameTextView: TextView = itemView.findViewById(R.id.mentorName)
        private val datePublishedTextView: TextView = itemView.findViewById(R.id.datePublished)

        fun bind(
            contribution: Contribution,
            context: Context,
            onItemClick: (Contribution) -> Unit,
            isEditable: Boolean
        ) {
            Picasso.get().load(contribution.imageUrl).into(imageView)
            titleTextView.text = contribution.title
            mentorNameTextView.text = "Published by: ${contribution.mentorName}"
            datePublishedTextView.text = contribution.datePublished

            itemView.setOnClickListener {
                onItemClick(contribution)
            }

            if (isEditable) {
                itemView.setOnLongClickListener {
                    showPopupMenu(context, it, contribution)
                    true
                }
            }
        }

        private fun showPopupMenu(context: Context, view: View, contribution: Contribution) {
            val popupMenu = PopupMenu(context, view)
            popupMenu.menuInflater.inflate(R.menu.job_item_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.update -> {
                        val intent = Intent(context, UpdateContribution::class.java).apply {
                            putExtra("id", contribution.id)
                            putExtra("contributionTitle", contribution.title)
                            putExtra("contributionDesc", contribution.content)
                        }
                        context.startActivity(intent)
                        true
                    }
                    R.id.delete -> {
                        showDeleteConfirmationDialog(contribution)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        private fun showDeleteConfirmationDialog(contribution: Contribution) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Contribution")
            builder.setMessage("Are you sure you want to delete this contribution?")
            builder.setPositiveButton("Yes") { _, _ ->
                deleteContribution(contribution.id)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.create().show()
        }

        private fun deleteContribution(contributionId: String) {
            val contributionsReference = FirebaseDatabase.getInstance().getReference("contributions")
            contributionsReference.child(contributionId).removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Optionally notify the adapter of the removal, but ListAdapter should handle it automatically
                } else {
                    // Handle failure
                }
            }
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

    fun filter(text: String) {
        val searchText = text.toLowerCase(Locale.getDefault())
        filteredList = if (searchText.isEmpty()) {
            contributionList
        } else {
            contributionList.filter { contribution ->
                contribution.title.toLowerCase(Locale.getDefault()).contains(searchText) ||
                        contribution.mentorName.toLowerCase(Locale.getDefault()).contains(searchText)
            }
        }
        submitList(filteredList)
    }
}

