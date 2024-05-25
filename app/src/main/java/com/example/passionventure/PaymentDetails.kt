package com.example.passionventure

import Payment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class PaymentDetails : AppCompatActivity() {

    private lateinit var bookingID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_details)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }

        bookingID = intent.getStringExtra("bookingID").toString()

        fetchDetails()
    }

    private fun fetchDetails() {
        val paymentRef = FirebaseDatabase.getInstance().getReference("payments")
        paymentRef.orderByChild("bookingID").equalTo(bookingID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (paymentSnapshot in dataSnapshot.children) {
                        val paymentDetails = paymentSnapshot.getValue(Payment::class.java)
                        if (paymentDetails != null) {
                            findViewById<TextView>(R.id.Receiver).text = "Receiver: ${paymentDetails.mentorName}"
                            findViewById<TextView>(R.id.Sender).text = "Sender: ${paymentDetails.currUser}"
                            findViewById<TextView>(R.id.paymentAmount).text = "Amount: ${paymentDetails.paymentAmount}"
                            findViewById<TextView>(R.id.paymentMethod).text = "Payment Method: ${paymentDetails.paymentMethod}"
                            findViewById<TextView>(R.id.status).text = "Status: Paid"
                            val imageView = findViewById<ImageView>(R.id.proofImg)
                            Picasso.get().load(paymentDetails.imageUrl).into(imageView)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Toast.makeText(applicationContext, "Failed to fetch payment details", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
