package com.example.passionventure.ui.matching

import Jobs
import Resume
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView
        currUser = arguments?.getString("username").toString()
        recyclerView.layoutManager = LinearLayoutManager(activity)
        jobList = mutableListOf()
        resumeList = mutableListOf() // Initialize resume list
        jobListAdapter = JobAdapter(requireContext(), jobList, resumeList) { job, resume ->
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
        }
        recyclerView.adapter = jobListAdapter

        fetchDataFromDatabase()

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
                jobListAdapter.notifyDataSetChanged() // Update the adapter with the new job list
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
}
