package com.example.passionventure

import Jobs
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrganizationHomePage : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private lateinit var progressBar: ProgressBar

    // Current user's data
    private var company: String = ""
    private var companyDescription: String = ""
    private var address: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization_home_page)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        // Reference to the "jobs" node in your database
        databaseReference = database.getReference("jobs")
        // Reference to the "users" node in your database
        userReference = database.getReference("users")

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val menuButton: ImageButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Initialize spinners
        val jobExpSpinner: Spinner = findViewById(R.id.jobExp)
        val jobAttainmentSpinner: Spinner = findViewById(R.id.jobAttainment)

        // Populate job experience list
        val jobExpList = mutableListOf(
            "Select job experience",
            "No experience",
            "1 year experience",
            "Less than 1 year experience",
            "2 years experience",
            "3 years experience",
            "More than 3 years experience"
        )

        // Populate job attainment list
        val jobAttainmentList = mutableListOf(
            "Select job attainment",
            "High School Graduate",
            "Undergraduate",
            "Bachelor's Degree",
            "Master's Degree",
            "Ph.D."
        )

        // Set adapter for Job Experience Spinner
        val jobExpAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            jobExpList
        )
        jobExpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobExpSpinner.adapter = jobExpAdapter

        // Set adapter for Job Attainment Spinner
        val jobAttainmentAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            jobAttainmentList
        )
        jobAttainmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobAttainmentSpinner.adapter = jobAttainmentAdapter

        progressBar = findViewById(R.id.progressBar)

        val postJobButton = findViewById<AppCompatButton>(R.id.button)
        postJobButton.setOnClickListener {
            saveJobToDatabase()
        }

        // Retrieve current user's name and description
        val username = intent.getStringExtra("username").toString()
        getUserData(username)

    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.main_menu, popupMenu.menu)

        // Find the items you want to hide
        val menuItem1 = popupMenu.menu.findItem(R.id.messages)
        val menuItem2 = popupMenu.menu.findItem(R.id.resources)
        // Hide them
        menuItem1.isVisible = false
        menuItem2.isVisible = false

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.profile -> {
                    val currUser = intent.getStringExtra("username")
                    val intent = Intent(this, ProfileView::class.java)
                    intent.putExtra("username", currUser)
                    startActivity(intent)
                    true
                }
                R.id.logout -> {
                    val intent = Intent(this, SignInView::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun saveJobToDatabase() {
        val jobTitleEditText = findViewById<TextInputEditText>(R.id.jobTitleEditText)
        val jobDescriptionEditText = findViewById<TextInputEditText>(R.id.jobDescriptionEditText)
        val jobCategoryEditText = findViewById<TextInputEditText>(R.id.jobCategoryEditText)
        val jobSalaryEditText = findViewById<TextInputEditText>(R.id.salaryEditText)
        val jobExpSpinner = findViewById<Spinner>(R.id.jobExp)
        val jobAttainmentSpinner = findViewById<Spinner>(R.id.jobAttainment)

        val jobTitle = jobTitleEditText.text.toString()
        val jobSalary = jobSalaryEditText.text.toString()
        val jobDescription = jobDescriptionEditText.text.toString()
        val jobCategory = jobCategoryEditText.text.toString()
        val jobExp = jobExpSpinner.selectedItem.toString()
        val jobAttainment = jobAttainmentSpinner.selectedItem.toString()

        if (jobTitle.isEmpty() || jobDescription.isEmpty() || jobCategory.isEmpty() || jobSalary.isEmpty() || jobExp == "Select job experience" || jobAttainment == "Select job attainment") {
            // Show toast indicating that all fields are required
            showToast("Please enter all fields")
            return
        }

        if (company.isEmpty() || companyDescription.isEmpty()) {
            // Show toast indicating that user data is not available
            showToast("User data not available")
            return
        }

        // Create a Jobs object with company and companyDescription
        val job = Jobs(jobTitle, jobDescription, jobCategory, jobExp, jobAttainment, company, companyDescription, jobSalary, address)

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Generate a unique key for the job
        val jobId = databaseReference.push().key

        if (jobId != null) {
            // Save the job details to the database under the generated jobId
            databaseReference.child(jobId).setValue(job)
                .addOnSuccessListener {
                    jobTitleEditText.setText("")
                    jobDescriptionEditText.setText("")
                    jobCategoryEditText.setText("")
                    jobSalaryEditText.setText("")
                    jobExpSpinner.setSelection(0)
                    jobAttainmentSpinner.setSelection(0)
                    showToast("Job posted successfully")
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener {
                    showToast("Failed to post job")
                    progressBar.visibility = View.GONE
                }
        }
    }

    private fun getUserData(username: String) {
        userReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userDataSnapshot = dataSnapshot.children.first()

                    company = userDataSnapshot.child("name").getValue(String::class.java) ?: ""
                    companyDescription = userDataSnapshot.child("description").getValue(String::class.java) ?: ""
                    address = userDataSnapshot.child("address").getValue(String::class.java) ?: ""
                } else {
                    company = ""
                    companyDescription = ""
                    address = ""
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}