package com.example.passionventure

import User
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

        val signInButton = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.button)
        signInButton.setOnClickListener {
            val username = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.userNameEt).text.toString().trim()
            val password = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.passET).text.toString().trim()

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
                                        startActivity(intent)

                                    } else if (user.role == "Mentor") {
                                        val intent = Intent(this@SignInView, EnthusiastHomePage::class.java)
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