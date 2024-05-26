package com.example.passionventure.ui.resources

import Contribution
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.passionventure.ContributionAdapter
import com.example.passionventure.ContributionDetails
import com.example.passionventure.databinding.FragmentResourcesBinding
import com.google.firebase.database.*

class ResourcesFragment : Fragment() {

    private var _binding: FragmentResourcesBinding? = null
    private lateinit var contributionsAdapter: ContributionAdapter
    private lateinit var contributionsRef: DatabaseReference
    private lateinit var contributionList: MutableList<Contribution>
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val resourcesViewModel =
            ViewModelProvider(this).get(ResourcesViewModel::class.java)

        _binding = FragmentResourcesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize Firebase database reference
        contributionsRef = FirebaseDatabase.getInstance().getReference("contributions")
        contributionList = mutableListOf()
        contributionsAdapter = ContributionAdapter(requireContext(), contributionList,
            { contribution ->
                val intent = Intent(requireContext(), ContributionDetails::class.java).apply {
                    putExtra("id", contribution.id)
                    putExtra("contributionTitle", contribution.title)
                    putExtra("contributionDesc", contribution.content)
                }
                startActivity(intent)
            },
            false // isEditable
        )

        binding.recyclerView.apply {
            adapter = contributionsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Fetch contributions
        fetchContributions()

        val searchEditText: EditText = binding.searchTab
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                contributionsAdapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        return root
    }



    private fun fetchContributions() {
        contributionsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                contributionList.clear()
                for (childSnapshot in snapshot.children) {
                    val contribution = childSnapshot.getValue(Contribution::class.java)
                    contribution?.let {
                        contributionList.add(it)
                    }
                }
                // Update adapter's list with fetched contributions
                contributionsAdapter.submitList(contributionList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load contributions", Toast.LENGTH_SHORT).show()
            }
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
