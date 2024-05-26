package com.example.passionventure.ui.addContribution

import Contribution
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.passionventure.databinding.FragmentAddContributionBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class addContributionFragment : Fragment() {

    private var _binding: FragmentAddContributionBinding? = null
    private lateinit var mentorName: String
    private lateinit var progressBar: ProgressBar
    private var imageUri: Uri? = null
    private lateinit var attachBtn: ImageButton
    private val PICK_FILE_REQUEST_CODE = 1
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mentorName = it.getString("username").toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContributionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Reference UI elements
        val titleEditText: EditText = binding.resourceEditText
        val contentEditText: EditText = binding.contentEditText
        val datePicker: DatePicker = binding.datePublished
        attachBtn = binding.attachBtn
        progressBar = binding.progressBar
        val addButton: Button = binding.button

        // Set click listener for attach button
        attachBtn.setOnClickListener {
            openFilePicker()
        }

        // Set click listener for add button
        addButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val content = contentEditText.text.toString().trim()
            val datePublished = getDateFromDatePicker(datePicker)

            // Check if title, content, and date are not empty
            if (title.isNotEmpty() && content.isNotEmpty() && datePublished.isNotEmpty() && imageUri != null) {
                // Check if the title already exists
                checkTitleAndUploadImage(title, content, datePublished)
            } else {
                // Show error message if any field is empty
                Toast.makeText(requireContext(), "Please fill in all fields and attach an image", Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Open file picker to select an image
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    // Handle result from file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            // Get the URI of the selected file
            imageUri = data?.data
            // Display the file name to the user
            binding.fileNameTextView.text = getFileName(imageUri!!)
        }
    }

    // Get the file name from the URI
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            requireActivity().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            // If DISPLAY_NAME column is not available, use the last segment of the URI
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut!!.plus(1))
            }
        }
        return result ?: "Unknown"
    }

    // Custom function to get date from DatePicker
    private fun getDateFromDatePicker(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1 // Month starts from 0
        val year = datePicker.year
        return "$day/$month/$year"
    }

    // Function to check title and upload image to Firebase Storage if title is unique
    private fun checkTitleAndUploadImage(title: String, content: String, datePublished: String) {
        progressBar.visibility = View.VISIBLE
        val contributionRef = FirebaseDatabase.getInstance().getReference("contributions")

        // Check if a contribution with the same title exists
        contributionRef.orderByChild("title").equalTo(title).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "A contribution with this title already exists", Toast.LENGTH_SHORT).show()
                } else {
                    // Title is unique, proceed to upload image
                    uploadImageToStorage(title, content, datePublished)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error checking title: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to upload image to Firebase Storage
    private fun uploadImageToStorage(title: String, content: String, datePublished: String) {
        val fileName = UUID.randomUUID().toString()
        val storageRef = FirebaseStorage.getInstance().getReference("contributions/$fileName")

        progressBar.visibility = View.VISIBLE // Show progress bar

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    saveContributionToDatabase(title, content, datePublished, uri.toString())
                    progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE // Hide progress bar on failure
            }
    }

    // Function to save contribution to database
    private fun saveContributionToDatabase(title: String, content: String, datePublished: String, imageUrl: String) {
        val contributionRef = FirebaseDatabase.getInstance().getReference("contributions")

        // Generate the key using push() method and save it to contributionId
        val contributionId = contributionRef.push().key ?: ""
        val contribution = Contribution(contributionId, mentorName, title, content, datePublished, imageUrl)

        contributionRef.child(contributionId).setValue(contribution)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Contribution saved successfully", Toast.LENGTH_SHORT).show()
                // Optionally, you can navigate to another fragment/activity or perform any other action
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to save contribution: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
