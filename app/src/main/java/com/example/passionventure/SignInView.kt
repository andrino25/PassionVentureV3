package com.example.passionventure

import User
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.FirebaseUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignInView : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    //New Code
    private lateinit var usernameEt: TextInputEditText
    private lateinit var passwordEt: TextInputEditText
    private lateinit var signInButton: androidx.appcompat.widget.AppCompatButton
    private lateinit var contactEt: String
    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("users")


        // Initialize views
        usernameEt = findViewById(R.id.userNameEt)
        passwordEt = findViewById(R.id.passET)
        signInButton = findViewById(R.id.button)

        // Initialize contactEt after lateinit var declaration
        contactEt = intent.getStringExtra("contact") ?: ""
        getUsername()

        val toSignUp = findViewById<TextView>(R.id.toSignUp)
        toSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }


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
        usernameEt.filters = arrayOf(emojiFilter)
        passwordEt.filters = arrayOf(emojiFilter)

        signInButton.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val username = usernameEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()

        if (username.isEmpty() || username.length < 3) {
            usernameEt.error = "Username length should be at least 3 chars"
            return
        }

        if (password.isEmpty()) {
            passwordEt.error = "Password cannot be empty"
            return
        }

        val userRef = databaseReference.orderByChild("username").equalTo(username)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user?.password == password) {
                            handleUserRole(user)
                            // Save the username globally
                            saveCurrentUser(username)
                            return
                        }
                    }
                    Toast.makeText(this@SignInView, "Invalid password, Please try again", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@SignInView, "User does not exist!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SignInView, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Update userModel with username if needed
        if (userModel != null) {
            userModel?.username = username
        } else {
            userModel = UserModel(contact = contactEt, username = username, createdTimestamp = Timestamp.now(), isCurrentUser = true)
        }
    }

    private fun handleUserRole(user: User) {
        when (user.role) {
            "Enthusiast" -> {
                val intent = Intent(this, EnthusiastHomePage::class.java)
                intent.putExtra("username", user.username)
                intent.putExtra("name", user.name)
                intent.putExtra("userProfile", user.profileImageUrl)
                Toast.makeText(this, "Welcome Enthusiast ${user.name}", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
            "Mentor" -> {
                val intent = Intent(this, MentorHomePage::class.java)
                intent.putExtra("username", user.username)
                intent.putExtra("name", user.name)
                Toast.makeText(this, "Welcome Mentor ${user.name}", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
            "Organization" -> {
                val intent = Intent(this, CompanyHomePage::class.java)
                intent.putExtra("username", user.username)
                Toast.makeText(this, "Welcome ${user.name} Company", Toast.LENGTH_SHORT).show()
                startActivity(intent)
            }
            else -> {
                Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getUsername() {
        val userRef = FirebaseUtil.currentUserDetails()
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel::class.java)
                userModel?.let {
                    usernameEt.setText(it.username)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@SignInView, "Failed to load user details: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveCurrentUser(username: String) {
        // Save the current user's username in shared preferences
        val sharedPref = getSharedPreferences("com.example.passionventure.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("currentUsername", username)
            apply()
        }
    }

}
