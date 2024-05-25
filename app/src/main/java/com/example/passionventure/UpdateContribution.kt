package com.example.passionventure

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class UpdateContribution : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDesc: EditText
    private lateinit var buttonUpdateContribution: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_contribution)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("contributions")

        // Initialize views
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextDesc = findViewById(R.id.editTextDesc)
        buttonUpdateContribution = findViewById(R.id.buttonUpdateJob)

        // Retrieve and display passed data
        val contributionId = intent.getStringExtra("id")
        val contributionTitle = intent.getStringExtra("contributionTitle")
        val contributionDesc = intent.getStringExtra("contributionDesc")

        editTextTitle.setText(contributionTitle)
        editTextDesc.setText(contributionDesc)

        // Set click listener for the update button
        buttonUpdateContribution.setOnClickListener {
            val updatedTitle = editTextTitle.text.toString().trim()
            val updatedDesc = editTextDesc.text.toString().trim()

            if (contributionId != null) {
                updateContribution(contributionId, updatedTitle, updatedDesc)
            }
        }

        // Handle back button click
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun updateContribution(id: String, title: String, desc: String) {
        // Update the contribution in Firebase
        val contributionUpdates = mapOf<String, Any>(
            "title" to title,
            "content" to desc
        )

        database.child(id).updateChildren(contributionUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Contribution updated successfully", Toast.LENGTH_SHORT).show()
                finish() // Close the activity and go back to the previous screen
            } else {
                Toast.makeText(this, "Failed to update contribution", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
