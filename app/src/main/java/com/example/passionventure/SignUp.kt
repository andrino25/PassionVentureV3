package com.example.passionventure

import User
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class SignUp : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var profileImage: ShapeableImageView
    private var selectedImageUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val toSignIn = findViewById<TextView>(R.id.toSignIn)
        toSignIn.setOnClickListener {
            val intent = Intent(this, SignInView::class.java)
            startActivity(intent)
        }

        // Counter of words
        val descriptionEt = findViewById<TextInputEditText>(R.id.descriptionEt)
        val wordCountTextView = findViewById<TextView>(R.id.wordCountTextView)

        descriptionEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val words = if (descriptionEt.text?.isNotEmpty() == true) {
                    s?.trim()?.split("\\s+".toRegex())?.size ?: 0
                } else {
                    0
                }
                wordCountTextView.text = "$words/100" // Update the word count TextView

                if (words > 100) {
                    Toast.makeText(this@SignUp, "Maximum word limit reached", Toast.LENGTH_SHORT).show()
                    val lastSpaceIndex = s?.toString()?.lastIndexOf(" ")
                    // Remove the last word from the EditText
                    if (lastSpaceIndex != null && lastSpaceIndex != -1) {
                        val editable = descriptionEt.text
                        editable?.replace(lastSpaceIndex, s.length, "")
                        descriptionEt.setSelection(descriptionEt.text?.length ?: 0) // Move cursor to end
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })


        // Initialize Firebase references
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
        profileImage = findViewById(R.id.profileImage)
        progressBar = findViewById(R.id.progressBar)

        profileImage.setOnClickListener {
            openGalleryForImage()
        }

        val roleGroup = findViewById<RadioGroup>(R.id.roleGroup)
        val professionSpinner = findViewById<Spinner>(R.id.professionSpinner)
        val descriptionLayout = findViewById<TextInputLayout>(R.id.descriptionLayout)

        roleGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.mentorRButton) {
                professionSpinner.visibility = View.VISIBLE
                descriptionLayout.visibility = View.VISIBLE
                wordCountTextView.visibility = View.VISIBLE
            } else if (checkedId == R.id.organizationButton){
                professionSpinner.visibility = View.GONE
                descriptionLayout.visibility = View.VISIBLE
                wordCountTextView.visibility = View.VISIBLE
            }else {
                professionSpinner.visibility = View.GONE
                descriptionLayout.visibility = View.GONE
                wordCountTextView.visibility = View.GONE

            }
        }

        val professionsList = mutableListOf(
            "Select a profession",
            "Software Engineer",
            "Data Scientist",
            "Product Manager",
            "UX/UI Designer",
            "Digital Marketer",
            "Financial Analyst",
            "Project Manager",
            "Business Consultant",
            "Sales Manager",
            "Michelin Chef",
            "Human Resources Manager"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, professionsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        professionSpinner.adapter = adapter

        // Custom InputFilter to disallow emojis and show a toast message
        val emojiFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (index in start until end) {
                val type = Character.getType(source[index].toInt())
                if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                    return@InputFilter ""
                }
            }
            null
        }

        // Apply InputFilter to disallow emojis in input fields
        val nameEt = findViewById<TextInputEditText>(R.id.nameEt)
        val emailEt = findViewById<TextInputEditText>(R.id.emailEt)
        val contactEt = findViewById<TextInputEditText>(R.id.contactEt)
        val usernameEt = findViewById<TextInputEditText>(R.id.userNameEt)
        val passwordEt = findViewById<TextInputEditText>(R.id.passET)
        val confirmPasswordEt = findViewById<TextInputEditText>(R.id.confirmPassEt)
        val addressEt = findViewById<TextInputEditText>(R.id.addressEt)

        nameEt.filters = arrayOf(emojiFilter)
        emailEt.filters = arrayOf(emojiFilter)
        usernameEt.filters = arrayOf(emojiFilter)
        passwordEt.filters = arrayOf(emojiFilter)
        confirmPasswordEt.filters = arrayOf(emojiFilter)
        addressEt.filters = arrayOf(emojiFilter)
        descriptionEt.filters = arrayOf(emojiFilter)

        val signUpButton = findViewById<AppCompatButton>(R.id.button)
        signUpButton.setOnClickListener {
            val name = nameEt.text.toString().trim()
            val email = emailEt.text.toString().trim()
            val contact = contactEt.text.toString().trim()
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val address = addressEt.text.toString().trim()
            val confirmPassword = confirmPasswordEt.text.toString().trim()
            var description = descriptionEt.text.toString().trim()

            val userCategory = findViewById<RadioGroup>(R.id.roleGroup)

            if (userCategory.checkedRadioButtonId == -1) {
                Toast.makeText(this@SignUp, "Please select a role.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = findViewById<RadioButton>(userCategory.checkedRadioButtonId)
            val userCategoryText = selectedRadioButton.text.toString()

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || address.isEmpty()) {
                Toast.makeText(this@SignUp, "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this@SignUp, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedImageUri == null) {
                Toast.makeText(this@SignUp, "Please select a profile image.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userCategoryText == "Mentor") {
                if (professionSpinner.selectedItem.toString() == "Select a profession") {
                    Toast.makeText(this@SignUp, "Please select a valid profession.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (description.isEmpty()) {
                    Toast.makeText(this@SignUp, "Please enter a description.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else if (userCategoryText == "Organization") {
                if (description.isEmpty()) {
                    Toast.makeText(this@SignUp, "Please enter a description for your organization.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            } else {
                description = "None"
            }


            val profession = if (userCategoryText == "Mentor") {
                professionSpinner.selectedItem.toString()
            } else {
                "None"
            }

            databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            Toast.makeText(this@SignUp, "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show()
                        } else {
                            progressBar.visibility = View.VISIBLE
                            uploadImageToStorage(name, email, contact, username, password, userCategoryText, profession, address, description)
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

    private fun uploadImageToStorage(name: String, email: String, contact: String, username: String, password: String, userCategory: String, profession: String, address: String, description: String) {
        selectedImageUri?.let { uri ->
            val imageRef = storageReference.child("${System.currentTimeMillis()}_${uri.lastPathSegment}")
            val uploadTask = imageRef.putFile(uri)

            // Show progress bar before starting the upload task
            progressBar.visibility = View.VISIBLE

            uploadTask.addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val user = User(name, email, contact, username, password, userCategory, address, imageUrl, profession, description) // Include address and description in User object
                    databaseReference.push().setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this@SignUp, "Registration Successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUp, SignInView::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@SignUp, "Failed to register: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@SignUp, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
            }.addOnCompleteListener {
                // Hide progress bar after the upload task completes (whether successful or not)
                progressBar.visibility = View.GONE
            }
        }
    }

    // Function to check if the input contains emojis
    private fun containsEmoji(text: String): Boolean {
        val emojiRegex = Regex("[\\p{So}\\p{Cs}]")
        return emojiRegex.containsMatchIn(text)
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
