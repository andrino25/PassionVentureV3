package com.example.passionventure.ui.matching

import Jobs
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.JobAdapter
import com.example.passionventure.databinding.FragmentMatchingBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MatchingFragment : Fragment() {

    private var _binding: FragmentMatchingBinding? = null
    private lateinit var jobList: MutableList<Jobs>
    private val binding get() = _binding!!

    private lateinit var jobListAdapter: JobAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        jobList = mutableListOf()
        jobListAdapter = JobAdapter(requireContext(), jobList)
        recyclerView.adapter = jobListAdapter


        fetchDataFromDatabase()

        val searchEditText: EditText = binding.searchTab
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                jobListAdapter.filter(s.toString())
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
                jobListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

}
