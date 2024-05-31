package com.example.passionventure

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.adapter.SearchUserRecyclerAdapter
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.FirebaseUtil
import com.firebase.ui.database.FirebaseRecyclerOptions

class SearchUserActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView

    private var adapter: SearchUserRecyclerAdapter? = null
    private var currentUsername: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)


        searchInput = findViewById(R.id.seach_username_input)
        searchButton = findViewById(R.id.search_user_btn)
        backButton = findViewById(R.id.back_btn)
        recyclerView = findViewById(R.id.search_user_recycler_view)

        searchInput.requestFocus()

        backButton.setOnClickListener {
            onBackPressed()
        }

        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString().trim()
            if (searchTerm.isEmpty() || searchTerm.length < 3) {
                searchInput.error = "Invalid Username"
                return@setOnClickListener
            }
            setupSearchRecyclerView(searchTerm)
        }

        // Retrieve current username from shared preferences
        val sharedPref = getSharedPreferences("com.example.passionventure.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        currentUsername = sharedPref.getString("currentUsername", null)
    }

    private fun setupSearchRecyclerView(searchTerm: String) {
        val query = FirebaseUtil.allUserCollectionReference()
            .orderByChild("username")
            .startAt(searchTerm)
            .endAt(searchTerm + '\uf8ff')

        val options: FirebaseRecyclerOptions<UserModel> = FirebaseRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java)
            .build()

        adapter = SearchUserRecyclerAdapter(options, applicationContext, currentUsername)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter?.startListening()
    }
}