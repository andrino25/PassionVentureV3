package com.example.passionventure

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView
import com.squareup.picasso.Picasso

class MentorDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mentor_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        val name = intent.getStringExtra("name")
        val profession = intent.getStringExtra("profession")
        val description = intent.getStringExtra("description")
        val imageUrl = intent.getStringExtra("imageUrl")

        val nameTextView: TextView = findViewById(R.id.mentor_name)
        val professionTextView: TextView = findViewById(R.id.mentor_profession)
        val descriptionTextView: TextView = findViewById(R.id.mentor_description)
        val imageView: ShapeableImageView = findViewById(R.id.mentor_image)
        val txt1: TextView = findViewById(R.id.titleDesc)

        txt1.text = "About $name"
        nameTextView.text = name
        professionTextView.text = profession
        descriptionTextView.text = description
        Picasso.get().load(imageUrl).into(imageView)


    }
}