package com.example.passionventure

import Jobs
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.passionventure.ui.offeredJobs.offeredJobsFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UpdateJobActivity : AppCompatActivity() {

    private lateinit var editTextJobTitle: EditText
    private lateinit var editTextJobDesc: EditText
    private lateinit var editTextJobCompany: EditText
    private lateinit var editTextJobCategory: EditText
    private lateinit var editTextJobExperience: EditText
    private lateinit var editTextJobAttainment: EditText
    private lateinit var editTextJobSalary: EditText
    private lateinit var editTextJobAddress: EditText
    private lateinit var editTextCompanyDescription: EditText
    private lateinit var buttonUpdateJob: Button
    private lateinit var jobKey: String
    private lateinit var oldJobTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_job)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        editTextJobTitle = findViewById(R.id.editTextJobTitle)
        editTextJobDesc = findViewById(R.id.editTextJobDesc)
        editTextJobCompany = findViewById(R.id.editTextJobCompany)
        editTextJobCategory = findViewById(R.id.editTextJobCategory)
        editTextJobExperience = findViewById(R.id.editTextJobExperience)
        editTextJobAttainment = findViewById(R.id.editTextJobAttainment)
        editTextJobSalary = findViewById(R.id.editTextJobSalary)
        editTextJobAddress = findViewById(R.id.editTextJobAddress)
        editTextCompanyDescription = findViewById(R.id.editTextCompanyDescription)
        buttonUpdateJob = findViewById(R.id.buttonUpdateJob)

        oldJobTitle = intent.getStringExtra("jobTitle").toString()
        editTextJobTitle.setText(intent.getStringExtra("jobTitle"))
        editTextJobDesc.setText(intent.getStringExtra("jobDesc"))
        editTextJobCompany.setText(intent.getStringExtra("jobCompany"))
        editTextJobCategory.setText(intent.getStringExtra("jobCategory"))
        editTextJobExperience.setText(intent.getStringExtra("jobExperience"))
        editTextJobAttainment.setText(intent.getStringExtra("jobAttainment"))
        editTextJobSalary.setText(intent.getStringExtra("jobSalary"))
        editTextJobAddress.setText(intent.getStringExtra("jobAddress"))
        editTextCompanyDescription.setText(intent.getStringExtra("companyDescription"))

        getJobKey(oldJobTitle)
        buttonUpdateJob.setOnClickListener {
            // Get the updated job details
            val updatedJob = Jobs(
                editTextJobTitle.text.toString(),
                editTextJobDesc.text.toString(),
                editTextJobCategory.text.toString(),
                editTextJobExperience.text.toString(),
                editTextJobAttainment.text.toString(),
                editTextJobCompany.text.toString(),
                editTextCompanyDescription.text.toString(),
                editTextJobSalary.text.toString(),
                editTextJobAddress.text.toString()
            )

            // Update the job in the database
            updateJob(updatedJob)
        }
    }

    private fun updateJob(job: Jobs) {
        // Get a reference to the Firebase database
        val jobReference = FirebaseDatabase.getInstance().getReference("jobs")

        // Check if the job ID is not blank or null
        if (jobKey.isNotBlank()) {
            // Get the reference to the specific job node using the job ID
            val specificJobReference = jobReference.child(jobKey)

            // Update the job with the new details
            specificJobReference.setValue(job).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@UpdateJobActivity, "Job updated successfully", Toast.LENGTH_SHORT).show()
                    finish()

                } else {
                    // Failed to update job
                    Toast.makeText(this@UpdateJobActivity, "Failed to update job", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // Job ID is blank or null
            Toast.makeText(this@UpdateJobActivity, "Job ID is invalid", Toast.LENGTH_SHORT).show()
        }
    }



    private fun getJobKey(oldJobTitle: String) {
        val jobReference = FirebaseDatabase.getInstance().getReference("jobs")
        jobReference.orderByChild("title").equalTo(oldJobTitle).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                         jobKey = snapshot.key.toString()
                    }
                } else {
                    // Job with old title not found
                    Toast.makeText(this@UpdateJobActivity, "Job with old title not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(this@UpdateJobActivity, "Error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
