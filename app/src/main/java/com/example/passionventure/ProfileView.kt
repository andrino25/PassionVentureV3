package com.example.passionventure

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.app.ProgressDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.imageview.ShapeableImageView

class ProfileView : AppCompatActivity() {

    private lateinit var nameText: EditText
    private lateinit var emailText: EditText
    private lateinit var numText: EditText
    private lateinit var addEdit: EditText
    private lateinit var nameEditBtn: Button
    private lateinit var emailEditBtn: Button
    private lateinit var numEditBtn: Button
    private lateinit var addEditBtn: Button
    private lateinit var profileImage: ShapeableImageView
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var username: String
    private lateinit var descriptionText: EditText
    private lateinit var editDescriptionBtn: Button
    private lateinit var descriptionLayout: RelativeLayout
    private lateinit var wordCountTextView: TextView
    private lateinit var progressBar: ProgressBar

    private var imageUrl: String? = null
    private var storageReference: StorageReference? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val backBtn = findViewById<Button>(R.id.BtnBack)
        backBtn.setOnClickListener {
            onBackPressed()
        }


        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        numText = findViewById(R.id.numText)
        addEdit = findViewById(R.id.addEdit)
        profileImage = findViewById(R.id.imageView)
        saveButton = findViewById(R.id.saveButton)
        progressDialog = ProgressDialog(this)
        descriptionText = findViewById(R.id.descriptionEditText)
        editDescriptionBtn = findViewById(R.id.editDescription)
        descriptionLayout = findViewById(R.id.layout5)
        progressBar = findViewById(R.id.progressBar)

        nameEditBtn = findViewById(R.id.nameEdit)
        emailEditBtn = findViewById(R.id.emailEdit)
        numEditBtn = findViewById(R.id.numEdit)
        addEditBtn = findViewById(R.id.editAdd)
        selectImageButton = findViewById(R.id.selectImageButton)

        //word counter being implemented
        wordCountTextView = findViewById(R.id.wordCountTextView)
        descriptionText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val words = s?.trim()?.split("\\s+".toRegex())?.size ?: 0
                wordCountTextView.text = "$words/100" // Update the word count TextView

                // Display a toast if the word count exceeds 100
                if (words > 100) {
                    Toast.makeText(this@ProfileView, "Maximum word limit reached", Toast.LENGTH_SHORT).show()
                    // Remove the last word to ensure the word count stays within the limit
                    val lastSpaceIndex: Int? = s?.toString()?.lastIndexOf(" ")
                    if (lastSpaceIndex != -1 && lastSpaceIndex != null) {
                        descriptionText.setText(s?.delete(lastSpaceIndex, s.length))
                        descriptionText.setSelection(descriptionText.text?.length ?: 0) // Move cursor to end
                    }

                }
            }
        })


        //use to get thhe username of thhhe user para ma retrieve and iyang corresponding data
        username = intent.getStringExtra("username").toString()

        // Initialize Firebase Storage reference
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")

        // Firebase database reference
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userDataSnapshot = dataSnapshot.children.first()

                    val name = userDataSnapshot.child("name").getValue(String::class.java)
                    val email = userDataSnapshot.child("email").getValue(String::class.java)
                    val phoneNumber = userDataSnapshot.child("contact").getValue(String::class.java)
                    val address = userDataSnapshot.child("address").getValue(String::class.java)
                    imageUrl = userDataSnapshot.child("profileImageUrl").getValue(String::class.java)
                    val role = userDataSnapshot.child("role").getValue(String::class.java)
                    val description = userDataSnapshot.child("description").getValue(String::class.java)

                    nameText.setText(name)
                    emailText.setText(email)
                    numText.setText(phoneNumber)
                    addEdit.setText(address)

                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImage)
                    }

                    if (role == "Mentor") {
                        descriptionLayout.visibility = View.VISIBLE
                        descriptionText.setText(description)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })

        // Add click listeners to edit buttons
        nameEditBtn.setOnClickListener {
            nameText.isEnabled = true
            Toast.makeText(this@ProfileView, "Name is editable.", Toast.LENGTH_SHORT).show()
        }

        emailEditBtn.setOnClickListener {
            emailText.isEnabled = true
            Toast.makeText(this@ProfileView, "Email is editable.", Toast.LENGTH_SHORT).show()
        }

        numEditBtn.setOnClickListener {
            numText.isEnabled = true
            Toast.makeText(this@ProfileView, "Contact Number is editable.", Toast.LENGTH_SHORT).show()
        }

        addEditBtn.setOnClickListener {
            addEdit.isEnabled = true
            Toast.makeText(this@ProfileView, "Address is editable.", Toast.LENGTH_SHORT).show()
        }

        editDescriptionBtn.setOnClickListener {
            descriptionText.isEnabled = true
            Toast.makeText(this@ProfileView, "Description is editable.", Toast.LENGTH_SHORT).show()
        }

        selectImageButton.setOnClickListener {
            profileImage.isClickable = true
            Toast.makeText(this@ProfileView, "Profile Image is now editable.", Toast.LENGTH_SHORT).show()

            profileImage.setOnClickListener {
                if (profileImage.isClickable) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, PICK_IMAGE_REQUEST)
                }
            }
        }

        saveButton.setOnClickListener {

            progressBar.visibility = View.VISIBLE

            val name = nameText.text.toString().trim()
            val email = emailText.text.toString().trim()
            val phoneNumber = numText.text.toString().trim()
            val address = addEdit.text.toString().trim()
            val description = descriptionText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
                Toast.makeText(this@ProfileView, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                imageUrl?.let { currentImageUrl ->
                    deleteCurrentImage(currentImageUrl) {
                        uploadImageToStorage(selectedImageUri!!) { uploadedImageUrl ->
                            updateUserProfile(name, email, phoneNumber, address, description, uploadedImageUrl)
                        }
                    }
                } ?: run {
                    uploadImageToStorage(selectedImageUri!!) { uploadedImageUrl ->
                        updateUserProfile(name, email, phoneNumber, address, description, uploadedImageUrl)
                    }
                }
            } else {
                updateUserProfile(name, email, phoneNumber, address, description, imageUrl)
            }


            nameText.isEnabled = false
            numText.isEnabled = false
            emailText.isEnabled = false
            addEdit.isEnabled = false
            descriptionText.isEnabled = false
            profileImage.isClickable = false
        }
    }

    private fun deleteCurrentImage(imageUrl: String, onComplete: () -> Unit) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageRef.delete().addOnSuccessListener {
            onComplete()
        }.addOnFailureListener {
            onComplete() // Proceed even if deletion fails
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, onComplete: (String) -> Unit) {
        val fileReference = storageReference?.child("$username.jpg")
        fileReference?.putFile(imageUri)?.addOnSuccessListener {
            fileReference.downloadUrl.addOnSuccessListener { uri ->
                onComplete(uri.toString())
            }
        }?.addOnFailureListener {
            onComplete("")
        }
    }

    private fun updateUserProfile(name: String, email: String, phoneNumber: String, address: String, description: String, imageUrl: String?) {
        val userMap = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "contact" to phoneNumber,
            "address" to address,
            "description" to description
        )
        imageUrl?.let {
            userMap["profileImageUrl"] = it
        }

        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userSnapshot = dataSnapshot.children.first()
                    userSnapshot.ref.updateChildren(userMap).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@ProfileView, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
                            progressBar.visibility = View.INVISIBLE
                        } else {
                            Toast.makeText(this@ProfileView, "Failed to update profile.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            profileImage.setImageURI(selectedImageUri)
        }
    }
}
