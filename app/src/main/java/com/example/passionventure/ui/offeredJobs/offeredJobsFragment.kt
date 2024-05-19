package com.example.passionventure.ui.offeredJobs

import Jobs
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.JobAdapter
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

    // This property is only valid between onCreateView and
    // onDestroyView.
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

        // Retrieve user's name based on username
        username?.let { retrieveUserName(it) }

        // Initialize RecyclerView and adapter
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        jobList = mutableListOf()
        jobAdapter = JobAdapter(requireContext(), jobList)
        recyclerView.adapter = jobAdapter

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
        // Ensure that the binding is not null before accessing its properties
        _binding?.apply {
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
                        jobAdapter.notifyDataSetChanged()

                        if (jobList.isEmpty()) {
                            textViewNoItems.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            textViewNoItems.visibility = View.GONE
                            recyclerView.visibility = View.VISIBLE
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }

}
