package com.example.passionventure.ui.contributions

import Contribution
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passionventure.ContributionAdapter
import com.example.passionventure.ContributionDetails
import com.example.passionventure.ResumeList
import com.example.passionventure.databinding.FragmentContributionsBinding
import com.google.firebase.database.*

class ContributionsFragment : Fragment() {

    private var _binding: FragmentContributionsBinding? = null
    private lateinit var contributionsAdapter: ContributionAdapter
    private lateinit var contributionsRef: DatabaseReference
    private var mentorName: String? = null
    private lateinit var contributionList: MutableList<Contribution>
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mentorName = it.getString("username")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContributionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        contributionsRef = FirebaseDatabase.getInstance().getReference("contributions")

        setupRecyclerView()

        // Query contributions based on mentorName
        mentorName?.let { fetchContributions(it) }

        return root
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with context and click listener
        contributionsAdapter = ContributionAdapter(requireContext(),
            { contribution ->
            val intent = Intent(requireContext(), ContributionDetails::class.java).apply {
                putExtra("id", contribution.id)
            }
            startActivity(intent)
        }, true)  // isEditable = true

        binding.recyclerView.apply {
            adapter = contributionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun fetchContributions(mentorName: String) {
        contributionsRef.orderByChild("mentorName").equalTo(mentorName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val contributions = mutableListOf<Contribution>()
                    for (childSnapshot in snapshot.children) {
                        val contribution = childSnapshot.getValue(Contribution::class.java)
                        contribution?.let {
                            contributions.add(it)
                        }
                    }
                    contributionsAdapter.submitList(contributions)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
