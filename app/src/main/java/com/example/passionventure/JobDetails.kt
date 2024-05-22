package com.example.passionventure

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class JobDetails : AppCompatActivity() {

    private lateinit var jobTitle: String
    private lateinit var jobCompany: String
    private lateinit var username: String
    private val PICK_RESUME_REQUEST = 1
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        jobTitle = intent.getStringExtra("jobTitle") ?: ""
        val jobDesc = intent.getStringExtra("jobDesc")
        jobCompany = intent.getStringExtra("jobCompany") ?: ""
        val jobCategory = intent.getStringExtra("jobCategory")
        val jobExperience = intent.getStringExtra("jobExperience")
        val jobAttainment = intent.getStringExtra("jobAttainment")
        val jobSalary = intent.getStringExtra("jobSalary")
        val jobAddress = intent.getStringExtra("jobAddress")
        val companyDescription = intent.getStringExtra("companyDescription")
        username = intent.getStringExtra("username") ?: "" // Retrieve the username

        val jobDescTextView: TextView = findViewById(R.id.jobDesc)
        val jobTitleTextView: TextView = findViewById(R.id.jobTitle)
        val jobCompanyTextView: TextView = findViewById(R.id.jobCompany)
        val jobCategoryTextView: TextView = findViewById(R.id.jobCategory)
        val jobExperienceTextView: TextView = findViewById(R.id.jobExperience)
        val jobAttainmentTextView: TextView = findViewById(R.id.jobAttainment)
        val jobSalaryTextView: TextView = findViewById(R.id.jobSalary)
        val jobAddressTextView: TextView = findViewById(R.id.jobAddress)
        val companyDescriptionTextView: TextView = findViewById(R.id.companyDescription)

        jobTitleTextView.text = jobTitle
        jobDescTextView.text = jobDesc
        jobCompanyTextView.text = "✅ $jobCompany"
        jobCategoryTextView.text = "⚫ $jobCategory"
        jobExperienceTextView.text = "\uD83D\uDD34 $jobExperience"
        jobAttainmentTextView.text = "\uD83D\uDD35 $jobAttainment"
        jobSalaryTextView.text = "$jobSalary PHP"
        jobAddressTextView.text = jobAddress
        companyDescriptionTextView.text = companyDescription

        progressBar = findViewById(R.id.progressBar) // Initialize the progress bar

        val attachResumeButton: Button = findViewById(R.id.attachResumeButton)
        attachResumeButton.setOnClickListener {
            pickResume()
        }
    }

    private fun pickResume() {
        // Check if a resume already exists for the current user and job
        val databaseRef = FirebaseDatabase.getInstance().reference.child("resumes")
        databaseRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var resumeAlreadyExists = false
                    for (snapshot in dataSnapshot.children) {
                        val jobCompany = snapshot.child("jobCompany").getValue(String::class.java)
                        val jobTitle = snapshot.child("jobTitle").getValue(String::class.java)
                        if (jobCompany == this@JobDetails.jobCompany && jobTitle == this@JobDetails.jobTitle) {
                            resumeAlreadyExists = true
                            break
                        }
                    }
                    if (resumeAlreadyExists) {
                        Toast.makeText(this@JobDetails, "You have already submitted a resume for this job.", Toast.LENGTH_SHORT).show()
                    } else {
                        // If resume doesn't exist, allow the user to pick a new resume
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "*/*"
                        startActivityForResult(intent, PICK_RESUME_REQUEST)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_RESUME_REQUEST && resultCode == Activity.RESULT_OK) {
            val resumeUri = data?.data
            if (resumeUri != null) {
                uploadResume(resumeUri)
            }
        }
    }

    private fun uploadResume(uri: Uri) {
        progressBar.visibility = View.VISIBLE // Show the progress bar

        val storageRef = FirebaseStorage.getInstance().reference
        val resumeFilename = "Applicant_$username" // Customize the resume filename
        val resumeRef = storageRef.child("resumes").child(jobCompany).child(jobTitle).child(resumeFilename)

        resumeRef.putFile(uri)
            .addOnSuccessListener { _ ->
                resumeRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveResumeInfo(downloadUrl.toString())
                    progressBar.visibility = View.GONE // Hide the progress bar after successful upload
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to upload resume: ${exception.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE // Hide the progress bar if upload fails
            }
    }
    private fun saveResumeInfo(downloadUrl: String) {
        val databaseRef = FirebaseDatabase.getInstance().reference.child("resumes").push()
        val resumeInfo = mapOf(
            "jobCompany" to jobCompany,
            "jobTitle" to jobTitle,
            "resumeUrl" to downloadUrl,
            "username" to username // Include the username in the resume info
        )
        databaseRef.setValue(resumeInfo).addOnSuccessListener {
            Toast.makeText(this, "Resume uploaded successfully", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to save resume info: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
