package com.example.passionventure

import Resume
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResumeList : AppCompatActivity() {

    private lateinit var resumesReference: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var resumeAdapter: ResumeAdapter
    private var resumeList: MutableList<Resume> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume_list)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        recyclerView = findViewById(R.id.recyclerViewResumes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        resumeAdapter = ResumeAdapter(this, resumeList)
        recyclerView.adapter = resumeAdapter

        val jobTitle = intent.getStringExtra("jobTitle")
        if (jobTitle != null) {
            fetchResumesForJob(jobTitle)
        }
    }

    private fun fetchResumesForJob(jobTitle: String) {
        resumesReference = FirebaseDatabase.getInstance().getReference("resumes")
        resumesReference.orderByChild("jobTitle").equalTo(jobTitle)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    resumeList.clear()
                    for (resumeSnapshot in dataSnapshot.children) {
                        val resume = resumeSnapshot.getValue(Resume::class.java)
                        resume?.let {
                            resumeList.add(it)
                        }
                    }
                    resumeAdapter.notifyDataSetChanged()

                    // Show or hide the "No applicants" message based on the list size
                    if (resumeList.isEmpty()) {
                        findViewById<TextView>(R.id.noApplicantsTextView).visibility = View.VISIBLE
                    } else {
                        findViewById<TextView>(R.id.noApplicantsTextView).visibility = View.GONE
                    }
                }


                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }
}