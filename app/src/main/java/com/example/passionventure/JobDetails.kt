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

        val jobDesc = intent.getStringExtra("jobDesc")
        val jobCompany = intent.getStringExtra("jobCompany")
        val jobCategory = intent.getStringExtra("jobCategory")

        val jobDescTextView: TextView = findViewById(R.id.jobDesc)
        val jobCompanyTextView: TextView = findViewById(R.id.jobCompany)
        val jobCategoryTextView: TextView = findViewById(R.id.jobCategory)

        jobDescTextView.text = jobDesc
        jobCompanyTextView.text = jobCompany
        jobCategoryTextView.text = jobCategory
    }
}