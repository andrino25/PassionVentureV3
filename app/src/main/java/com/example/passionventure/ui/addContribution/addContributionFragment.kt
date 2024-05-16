package com.example.passionventure.ui.addContribution

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.passionventure.databinding.FragmentAddContributionBinding

class addContributionFragment : Fragment() {

    private var _binding: FragmentAddContributionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val addContributionViewModel =
            ViewModelProvider(this).get(addContributionViewModel::class.java)

        _binding = FragmentAddContributionBinding.inflate(inflater, container, false)
        val root: View = binding.root


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}