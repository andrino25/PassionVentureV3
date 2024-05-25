package com.example.passionventure

import Contribution
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ContributionDetails : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var mentorNameTextView: TextView
    private lateinit var datePublishedTextView: TextView
    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contribution_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }


        imageView = findViewById(R.id.imageHolder)
        titleTextView = findViewById(R.id.title)
        mentorNameTextView = findViewById(R.id.mentorName)
        datePublishedTextView = findViewById(R.id.datePublished)
        contentTextView = findViewById(R.id.content)

        val contributionId = intent.getStringExtra("id")
        database = FirebaseDatabase.getInstance().getReference("contributions")

        // Fetch contribution details based on ID
        contributionId?.let { fetchContributionDetails(it) }
    }

    private fun fetchContributionDetails(id: String) {
        database.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val contribution = snapshot.getValue(Contribution::class.java)
                contribution?.let {
                    // Update the UI with the fetched details
                    Picasso.get().load(it.imageUrl).into(imageView)
                    titleTextView.text = it.title
                    mentorNameTextView.text = "Published by: ${it.mentorName}"
                    datePublishedTextView.text = "Date Published: ${it.datePublished}"
                    contentTextView.text = it.content
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}
