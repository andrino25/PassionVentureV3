package com.example.passionventure

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ApplicantDetails : AppCompatActivity() {

    private lateinit var resumeUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_applicant_details)

        val username = intent.getStringExtra("username")

        // Now, fetch the user details from the Firebase database using the username
        // and display them in the UI
        fetchUserDetails(username)

        fetchResumeUrl(username)

        // Set click listener for download resume button
        val downloadResumeBtn = findViewById<Button>(R.id.downloadResumeBtn)
        downloadResumeBtn.setOnClickListener {
            downloadResume()
        }
    }

    private fun fetchUserDetails(username: String?) {
        // Assuming you have a "users" node in your Firebase database
        val usersReference = FirebaseDatabase.getInstance().getReference("users")
        usersReference.orderByChild("name").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Iterate through the dataSnapshot to retrieve user details
                    for (snapshot in dataSnapshot.children) {
                        val address = snapshot.child("address").getValue(String::class.java)
                        val contact = snapshot.child("contact").getValue(String::class.java)
                        val description = snapshot.child("description").getValue(String::class.java)
                        val email = snapshot.child("email").getValue(String::class.java)
                        val name = snapshot.child("name").getValue(String::class.java)
                        val profileImageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                        // Now, display these details in your UI
                        displayUserDetails(address, contact, email, name, profileImageUrl)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun fetchResumeUrl(username: String?) {
        // Assuming you have a "resumes" node in your Firebase database
        val resumesReference = FirebaseDatabase.getInstance().getReference("resumes")
        resumesReference.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Iterate through the dataSnapshot to retrieve resume URL
                    for (snapshot in dataSnapshot.children) {
                        resumeUrl = snapshot.child("resumeUrl").getValue(String::class.java).toString()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }


    private fun displayUserDetails(address: String?, contact: String?,  email: String?, name: String?, profileImageUrl: String?) {
        // Example: Displaying the fetched user details in TextViews
        findViewById<TextView>(R.id.addressTextView).text = "Address: $address"
        findViewById<TextView>(R.id.contactTextView).text = "Contact No: $contact"
        findViewById<TextView>(R.id.emailTextView).text = "Email: $email"
        findViewById<TextView>(R.id.nameTextView).text = "Applicant: $name"

        Picasso.get().load(profileImageUrl).into(findViewById<ShapeableImageView>(R.id.profileImageView))
    }

    private fun downloadResume() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl))
        startActivity(intent)
    }

}
