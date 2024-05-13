package com.example.passionventure

import User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class SignUp : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ShapeableImageView
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar : ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        profileImage = findViewById(R.id.profileImage)
        progressBar = findViewById(R.id.progressBar)

        profileImage.setOnClickListener {
            openGalleryForImage()
        }



        // Sign Up Button and other views setup...

        val signUpButton = findViewById<AppCompatButton>(R.id.button)
        signUpButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            val name = findViewById<TextInputEditText>(R.id.nameEt).text.toString().trim()
            val email = findViewById<TextInputEditText>(R.id.emailEt).text.toString().trim()
            val contact = findViewById<TextInputEditText>(R.id.contactEt).text.toString().trim()
            val username = findViewById<TextInputEditText>(R.id.userNameEt).text.toString().trim()
            val password = findViewById<TextInputEditText>(R.id.passET).text.toString().trim()
            val confirmPassword = findViewById<TextInputEditText>(R.id.confirmPassEt).text.toString().trim()
            val userCategory = findViewById<RadioGroup>(R.id.roleGroup)

            // Check if any radio button is selected
            if (userCategory.checkedRadioButtonId == -1) {
                Toast.makeText(this@SignUp, "Please select a role.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(userCategory.checkedRadioButtonId)
            val userCategoryText = selectedRadioButton.text.toString()

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@SignUp, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this@SignUp, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the username already exists in the database
            databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            // Username already exists
                            Toast.makeText(this@SignUp, "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Username doesn't exist, proceed with registration
                            // Upload the image to Firebase Storage
                            uploadImageToStorage(name, email, contact, username, password, userCategoryText)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SignUp, "Database Error!", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            profileImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToStorage(name: String, email: String, contact: String, username: String, password: String, userCategory: String) {
        selectedImageUri?.let { uri ->
            progressBar.visibility = View.VISIBLE
            val imageRef = storageReference.child("${System.currentTimeMillis()}_${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Image upload success
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Get the download URL
                    val imageUrl = uri.toString()
                    // Create a User object
                    val user = User(name, email, contact, username, password, userCategory, imageUrl)
                    // Push the user data to the Firebase Realtime Database
                    databaseReference.push().setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this@SignUp, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            // Navigate to sign in activity or do any other necessary action
                            val intent = Intent(this@SignUp, SignInView::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@SignUp, "Failed to register: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                // Image upload failed
                Toast.makeText(this@SignUp, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
