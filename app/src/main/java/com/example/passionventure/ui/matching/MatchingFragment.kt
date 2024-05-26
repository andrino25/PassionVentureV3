package com.example.passionventure.ui.matching

import Jobs
import Resume
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.JobAdapter
import com.example.passionventure.JobDetails
import com.example.passionventure.databinding.FragmentMatchingBinding
import com.google.firebase.database.*

class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private lateinit var jobList: MutableList<Jobs>
    private lateinit var resumeList: MutableList<Resume>
    private val binding get() = _binding!!
    private lateinit var jobListAdapter: JobAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var currUser: String
    private lateinit var noItemsTextView: TextView // Reference to the TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView
        noItemsTextView = binding.textViewNoItems // Initialize the TextView reference
        currUser = arguments?.getString("username").toString()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        jobList = mutableListOf()
        resumeList = mutableListOf() // Initialize resume list
        jobListAdapter = JobAdapter(requireContext(), jobList, resumeList, { job, resume ->
            // Handle item click inside the fragment
            val intent = Intent(requireContext(), JobDetails::class.java).apply {
                putExtra("jobTitle", job.title)
                putExtra("jobDesc", job.description)
                putExtra("jobCompany", job.company)
                putExtra("jobCategory", job.category)
                putExtra("jobExperience", job.experience)
                putExtra("jobAttainment", job.attainment)
                putExtra("jobSalary", job.salary)
                putExtra("jobAddress", job.companyAddress)
                putExtra("companyDescription", job.companyDescription)
                putExtra("username", currUser)
            }
            startActivity(intent)
        }, false) // Set isEditable to false
        recyclerView.adapter = jobListAdapter

        fetchDataFromDatabase()

        binding.searchTab.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                jobListAdapter.filter(s.toString()) // Apply filter based on search query
                // Update visibility of noItemsTextView based on filtered list size
                noItemsTextView.visibility = if (jobListAdapter.itemCount == 0) View.VISIBLE else View.INVISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchDataFromDatabase() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("jobs")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                jobList.clear()
                for (jobSnapshot in dataSnapshot.children) {
                    val job = jobSnapshot.getValue(Jobs::class.java)
                    job?.let {
                        jobList.add(it)
                    }
                }
                jobListAdapter.submitList(jobList)
                // Update visibility of noItemsTextView based on job list size
                noItemsTextView.visibility = if (jobList.isEmpty()) View.VISIBLE else View.INVISIBLE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}
