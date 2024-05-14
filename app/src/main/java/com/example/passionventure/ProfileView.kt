package com.example.passionventure

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.app.ProgressDialog
import android.widget.TextView

class ProfileView : AppCompatActivity() {

    private lateinit var nameText: EditText
    private lateinit var emailText: EditText
    private lateinit var numText: EditText
    private lateinit var addEdit: EditText
    private lateinit var nameEditBtn: Button
    private lateinit var emailEditBtn: Button
    private lateinit var numEditBtn: Button
    private lateinit var addEditBtn: Button
    private lateinit var profileImage: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var username: String

    private var imageUrl: String? = null
    private var storageReference: StorageReference? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        nameText = findViewById(R.id.nameText)
        emailText = findViewById(R.id.emailText)
        numText = findViewById(R.id.numText)
        addEdit = findViewById(R.id.addEdit)
        profileImage = findViewById(R.id.imageView)
        saveButton = findViewById(R.id.saveButton)
        progressDialog = ProgressDialog(this)

        nameEditBtn = findViewById(R.id.nameEdit)
        emailEditBtn = findViewById(R.id.emailEdit)
        numEditBtn = findViewById(R.id.numEdit)
        addEditBtn = findViewById(R.id.editAdd)
        selectImageButton = findViewById(R.id.selectImageButton)

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

                    nameText.setText(name)
                    emailText.setText(email)
                    numText.setText(phoneNumber)
                    addEdit.setText(address)

                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImage)
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

        selectImageButton.setOnClickListener {
            profileImage.isClickable = true
            Toast.makeText(this@ProfileView, "Profile Image is editable.", Toast.LENGTH_SHORT).show()
        }

        profileImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        saveButton.setOnClickListener {
            val name = nameText.text.toString().trim()
            val email = emailText.text.toString().trim()
            val phoneNumber = numText.text.toString().trim()
            val address = addEdit.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty()) {
                Toast.makeText(this@ProfileView, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToStorage(selectedImageUri!!) { uploadedImageUrl ->
                    updateUserProfile(name, email, phoneNumber, address, uploadedImageUrl)
                }
            } else {
                updateUserProfile(name, email, phoneNumber, address, imageUrl)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            profileImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadImageToStorage(imageUri: Uri, callback: (String) -> Unit) {
        val imageRef = storageReference!!.child(imageUri.lastPathSegment!!)
        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            Toast.makeText(this@ProfileView, "New image uploaded to storage.", Toast.LENGTH_SHORT).show()
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }.addOnFailureListener {
                Toast.makeText(this@ProfileView, "Failed to get download URL of the new image.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this@ProfileView, "Failed to upload new image to storage.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUserProfile(name: String, email: String, phoneNumber: String, address: String, imageUrl: String?) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        val userRef = databaseReference.orderByChild("username").equalTo(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (childSnapshot in dataSnapshot.children) {
                        childSnapshot.ref.child("name").setValue(name)
                        childSnapshot.ref.child("email").setValue(email)
                        childSnapshot.ref.child("contact").setValue(phoneNumber)
                        childSnapshot.ref.child("address").setValue(address)
                        childSnapshot.ref.child("profileImageUrl").setValue(imageUrl)

                        Toast.makeText(this@ProfileView, "User data updated.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ProfileView, "Failed to update user data.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
