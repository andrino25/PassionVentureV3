package com.example.passionventure

import Booking
import Payment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class Payments : AppCompatActivity() {

    private lateinit var attachBtn: ImageButton
    private lateinit var fileNameTextView: TextView
    private lateinit var paymentMethod: String
    private lateinit var mentorName: String
    private lateinit var paymentAmount: String
    private lateinit var bookingID: String
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

        // Initialize the views
        attachBtn = findViewById(R.id.attachBtn)
        fileNameTextView = findViewById(R.id.fileNameTextView)
        progressBar = findViewById(R.id.progressBar)
        val amountEditText = findViewById<EditText>(R.id.amountEditText)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val paymentBtn = findViewById<Button>(R.id.paymentBtn)

        // Retrieve data from the intent
        currentUser = intent.getStringExtra("currUser") ?: ""
        mentorName = intent.getStringExtra("mentorName") ?: ""
        bookingID = intent.getStringExtra("bookingID") ?: ""

        // Log the bookingID to verify
        Log.d("Payments", "Booking ID: $bookingID")

        // Set up the radio group listener
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            paymentMethod = when (checkedId) {
                R.id.cardBtn -> "Card"
                R.id.walletBtn -> "E-wallet / Digital Wallet"
                R.id.bankTransferBtn -> "Bank Transfer"
                else -> "Unknown"
            }
        }

        // Set up the attach button click listener
        attachBtn.setOnClickListener {
            openFilePicker()
        }

        // Set up the payment button click listener
        paymentBtn.setOnClickListener {
            paymentAmount = amountEditText.text.toString()

            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paymentAmount.isEmpty()) {
                Toast.makeText(this, "Please enter a payment amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Check if image is attached
            if (imageUri == null) {
                Toast.makeText(this, "Please attach an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
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

        val payment = Payment(currentUser, mentorName, paymentMethod, paymentAmount, imageUrl, bookingID)

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
        val bookingRef = FirebaseDatabase.getInstance().getReference("bookings").child(bookingID)
        bookingRef.child("bookingStatus").setValue("Completed")
            .addOnSuccessListener {
                Toast.makeText(this, "Booking status updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update booking status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
