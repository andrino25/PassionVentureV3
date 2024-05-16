package com.example.passionventure

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView

class JobDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }
        val jobTitle = intent.getStringExtra("jobTitle")
        val jobDesc = intent.getStringExtra("jobDesc")
        val jobCompany = intent.getStringExtra("jobCompany")
        val jobCategory = intent.getStringExtra("jobCategory")
        val jobExperience = intent.getStringExtra("jobExperience")
        val jobAttainment = intent.getStringExtra("jobAttainment")
        val jobSalary = intent.getStringExtra("jobSalary")
        val jobAddress = intent.getStringExtra("jobAddress")
        val companyDescription = intent.getStringExtra("companyDescription")

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
    }
}