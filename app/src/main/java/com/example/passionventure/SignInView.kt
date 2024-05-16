package com.example.passionventure

import User
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SignInView : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in_view)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("users")

        val toSignUp = findViewById<TextView>(R.id.toSignUp)
        toSignUp.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        val usernameEt = findViewById<TextInputEditText>(R.id.userNameEt)
        val passwordEt = findViewById<TextInputEditText>(R.id.passET)
        val signInButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.button)

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
            val username = usernameEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if the user exists in the database
            databaseReference.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (userSnapshot in snapshot.children) {
                                val user = userSnapshot.getValue(User::class.java)
                                if (user?.password == password) {
                                    if (user.role == "Enthusiast") {
                                        // Start EnthusiastHomePage activity
                                        val intent = Intent(this@SignInView, EnthusiastHomePage::class.java)
                                        intent.putExtra("username", user.username)
                                        Toast.makeText(this@SignInView, "Welcome Enthusiast ${user.name}", Toast.LENGTH_SHORT).show()
                                        startActivity(intent)
                                    } else if (user.role == "Mentor") {
                                        val intent = Intent(this@SignInView, MentorHomePage::class.java)
                                        intent.putExtra("username", user.username)
                                        Toast.makeText(this@SignInView, "Welcome Mentor ${user.name}", Toast.LENGTH_SHORT).show()
                                        startActivity(intent)
                                    }
                                    else if (user.role == "Organization") {
                                        val intent = Intent(this@SignInView, OrganizationHomePage::class.java)
                                        intent.putExtra("username", user.username)
                                        Toast.makeText(this@SignInView, "Welcome ${user.name} Company", Toast.LENGTH_SHORT).show()
                                        startActivity(intent)
                                    }
                                    return
                                }
                            }
                            Toast.makeText(this@SignInView, "Invalid password, Please try again", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@SignInView, "User does not exist!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@SignInView, "Database Error!", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
