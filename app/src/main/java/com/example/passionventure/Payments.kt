package com.example.passionventure

import Booking
import Payment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Payments : AppCompatActivity() {

    private lateinit var attachBtn: ImageButton
    private lateinit var fileNameTextView: TextView
    private lateinit var paymentMethod: String
    private lateinit var mentorName: String
    private lateinit var paymentAmount: String
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar
    private val PICK_FILE_REQUEST_CODE = 1
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)

        val backBtn = findViewById<ImageButton>(R.id.backButton)
        backBtn.setOnClickListener {
            onBackPressed()
        }
        paymentAmount = findViewById<EditText>(R.id.amountEditText).text.toString()
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            paymentMethod = when (checkedId) {
                R.id.cardBtn -> "Card"
                R.id.walletBtn -> "E-wallet / Digital Wallet"
                R.id.bankTransferBtn -> "Bank Transfer"
                else -> "Unknown"
            }
        }
        currentUser = intent.getStringExtra("currUser") ?: ""
        mentorName = intent.getStringExtra("mentorName") ?: ""
        attachBtn = findViewById(R.id.attachBtn)
        fileNameTextView = findViewById(R.id.fileNameTextView) // Make sure this ID is correct
        progressBar = findViewById(R.id.progressBar)

        attachBtn.setOnClickListener {
            openFilePicker()
        }

        findViewById<Button>(R.id.paymentBtn).setOnClickListener {
            paymentAmount = findViewById<EditText>(R.id.amountEditText).text.toString()
            if (imageUri != null) {
                uploadImageToStorage()
            } else {
                Toast.makeText(this, "Please attach an image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data?.data
            val fileName = imageUri?.let { getFileName(it) }
            fileNameTextView.text = fileName
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut!!.plus(1))
            }
        }
        return result ?: "Unknown"
    }

    private fun uploadImageToStorage() {
        val fileName = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().getReference("payments/$fileName")

        progressBar.visibility = ProgressBar.VISIBLE // Show progress bar

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    savePaymentDetailsToDatabase(uri.toString())
                    progressBar.visibility = ProgressBar.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = ProgressBar.GONE // Hide progress bar on failure
            }
    }

    private fun savePaymentDetailsToDatabase(imageUrl: String) {
        progressBar.visibility = ProgressBar.VISIBLE
        val paymentDetailsRef = FirebaseDatabase.getInstance().getReference("payments")
        val paymentId = paymentDetailsRef.push().key ?: ""

        val payment = Payment(currentUser, mentorName, paymentMethod, paymentAmount, imageUrl)

        paymentDetailsRef.child(paymentId).setValue(payment)
            .addOnSuccessListener {
                progressBar.visibility = ProgressBar.GONE
                updateBookingStatus()
                Toast.makeText(this, "Payment details saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save payment details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                progressBar.visibility = ProgressBar.GONE // Hide progress bar when saving is complete
            }
    }
    private fun updateBookingStatus() {
        val bookingRef = FirebaseDatabase.getInstance().getReference("bookings")
        bookingRef.orderByChild("mentorName").equalTo(mentorName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (bookingSnapshot in dataSnapshot.children) {
                        val booking = bookingSnapshot.getValue(Booking::class.java)
                        if (booking != null && booking.currUser == currentUser) {
                            bookingRef.child(bookingSnapshot.key!!).child("bookingStatus").setValue("Completed")
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Toast.makeText(applicationContext, "Failed to update booking status", Toast.LENGTH_SHORT).show()
                }
            })
    }

}
