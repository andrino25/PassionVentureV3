package com.example.passionventure.ui.mentor

import User
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.MentorAdapter
import com.example.passionventure.databinding.FragmentMentorsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MentorsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mentorsAdapter: MentorAdapter
    private lateinit var mentorsList: MutableList<User>
    private var _binding: FragmentMentorsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mentorsViewModel =
            ViewModelProvider(this).get(MentorsViewModel::class.java)

        _binding = FragmentMentorsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val currUser = activity?.intent?.getStringExtra("name").toString()
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        mentorsList = mutableListOf()
        mentorsAdapter = MentorAdapter(this, mentorsList, currUser)
        recyclerView.adapter = mentorsAdapter


        fetchMentorsFromDatabase()

        val textView: TextView = binding.textHome
        mentorsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val searchEditText: EditText = binding.searchTab
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mentorsAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Not needed
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMentorsFromDatabase() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("users")
        databaseReference.orderByChild("role").equalTo("Mentor")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mentorsList.clear()
                    for (userSnapshot in snapshot.children) {
                        val mentor = userSnapshot.getValue(User::class.java)
                        mentor?.let { mentorsList.add(it) }
                    }
                    mentorsAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }

}