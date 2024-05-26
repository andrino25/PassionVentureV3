package com.example.passionventure.ui.postJob

import Jobs
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.passionventure.databinding.FragmentPostJobBinding
import com.google.firebase.database.*

class postJobFragment : Fragment() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userReference: DatabaseReference
    private var company: String = ""
    private var companyDescription: String = ""
    private var address: String = ""

    private var _binding: FragmentPostJobBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostJobBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("jobs")
        userReference = database.getReference("users")

        // Initialize spinners
        val jobExpSpinner = binding.jobExp
        val jobAttainmentSpinner = binding.jobAttainment

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
            requireContext(),
            android.R.layout.simple_spinner_item,
            jobExpList
        )
        jobExpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobExpSpinner.adapter = jobExpAdapter

        // Set adapter for Job Attainment Spinner
        val jobAttainmentAdapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            jobAttainmentList
        )
        jobAttainmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobAttainmentSpinner.adapter = jobAttainmentAdapter

        val postJobButton = binding.button
        postJobButton.setOnClickListener {
            saveJobToDatabase()
        }

        // Retrieve current user's name and description
        val username = arguments?.getString("username")?: ""
        getUserData(username)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveJobToDatabase() {
        val jobTitleEditText = binding.jobTitleEditText
        val jobDescriptionEditText = binding.jobDescriptionEditText
        val jobCategoryEditText = binding.jobCategoryEditText
        val jobSalaryEditText = binding.salaryEditText
        val jobExpSpinner = binding.jobExp
        val jobAttainmentSpinner = binding.jobAttainment

        val jobTitle = jobTitleEditText.text.toString().trim()
        val jobSalary = jobSalaryEditText.text.toString().trim()
        val jobDescription = jobDescriptionEditText.text.toString().trim()
        val jobCategory = jobCategoryEditText.text.toString().trim()
        val jobExp = jobExpSpinner.selectedItem.toString()
        val jobAttainment = jobAttainmentSpinner.selectedItem.toString()

        if (jobTitle.isEmpty() || jobDescription.isEmpty() || jobCategory.isEmpty() || jobSalary.isEmpty() || jobExp == "Select job experience" || jobAttainment == "Select job attainment") {
            showToast("Please enter all fields")
            return
        }

        if (company.isEmpty() || companyDescription.isEmpty()) {
            showToast("User data not available")
            return
        }

        val job = Jobs(jobTitle, jobDescription, jobCategory, jobExp, jobAttainment, company, companyDescription, jobSalary, address)
        val jobRef = FirebaseDatabase.getInstance().getReference("jobs")

        // Check if a job with the same title exists
        jobRef.orderByChild("title").equalTo(jobTitle).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    showToast("A job with this title already exists")
                } else {
                    val jobId = jobRef.push().key
                    if (jobId != null) {
                        jobRef.child(jobId).setValue(job)
                            .addOnSuccessListener {
                                jobTitleEditText.setText("")
                                jobDescriptionEditText.setText("")
                                jobCategoryEditText.setText("")
                                jobSalaryEditText.setText("")
                                jobExpSpinner.setSelection(0)
                                jobAttainmentSpinner.setSelection(0)
                                showToast("Job posted successfully")
                            }
                            .addOnFailureListener {
                                showToast("Failed to post job")
                            }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("Error checking title: ${databaseError.message}")
            }
        })
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
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
