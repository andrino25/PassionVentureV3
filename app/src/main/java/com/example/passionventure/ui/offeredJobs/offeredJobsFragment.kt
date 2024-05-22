package com.example.passionventure.ui.offeredJobs

import Jobs
import Resume
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.JobAdapter
import com.example.passionventure.ResumeList
import com.example.passionventure.databinding.FragmentOfferedJobsBinding
import com.google.firebase.database.*

class offeredJobsFragment : Fragment() {

    private var _binding: FragmentOfferedJobsBinding? = null
    private lateinit var userReference: DatabaseReference
    private lateinit var jobsReference: DatabaseReference
    private var username: String = ""
    private var company: String = ""
    private lateinit var jobList: MutableList<Jobs>
    private lateinit var jobAdapter: JobAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var resumeList: MutableList<Resume> // Add resumeList

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val offeredJobsViewModel = ViewModelProvider(this).get(offeredJobsViewModel::class.java)

        _binding = FragmentOfferedJobsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textHome
        offeredJobsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        username = arguments?.getString("username").toString()

        // Initialize Firebase
        userReference = FirebaseDatabase.getInstance().getReference("users")
        jobsReference = FirebaseDatabase.getInstance().getReference("jobs")

        // Initialize RecyclerView and adapter
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        jobList = mutableListOf()
        resumeList = mutableListOf() // Initialize resume list
        jobAdapter = JobAdapter(requireContext(), jobList, resumeList) { job, resume ->
            val intent = Intent(requireContext(), ResumeList::class.java).apply {
            putExtra("jobTitle", job.title)
        }
            startActivity(intent)
        }
        recyclerView.adapter = jobAdapter

        // Fetch jobs and resumes
        retrieveUserName(username)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun retrieveUserName(username: String) {
        userReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userDataSnapshot = dataSnapshot.children.first()
                        company = userDataSnapshot.child("name").getValue(String::class.java) ?: ""
                        // Fetch jobs for the company
                        fetchJobsForCompany(company)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun fetchJobsForCompany(companyName: String) {
        jobsReference.orderByChild("company").equalTo(companyName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    jobList.clear()
                    for (jobSnapshot in dataSnapshot.children) {
                        val job = jobSnapshot.getValue(Jobs::class.java)
                        job?.let {
                            jobList.add(it)
                        }
                    }
                    // After fetching jobs, fetch resumes
                    fetchResumesForJobs(jobList)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun fetchResumesForJobs(jobs: List<Jobs>) {
        val resumeReference = FirebaseDatabase.getInstance().getReference("resumes")
        resumeReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                resumeList.clear()
                for (resumeSnapshot in dataSnapshot.children) {
                    val resume = resumeSnapshot.getValue(Resume::class.java)
                    resume?.let {
                        resumeList.add(it)
                    }
                }
                // After fetching resumes, update the adapter with the new resume list
                jobAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
