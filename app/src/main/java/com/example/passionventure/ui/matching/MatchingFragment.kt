package com.example.passionventure.ui.matching

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.JobDetails
import com.example.passionventure.JobItem
import com.example.passionventure.JobListAdapter
import com.example.passionventure.databinding.FragmentMatchingBinding

class MatchingFragment : Fragment(), JobListAdapter.OnItemClickListener {

    private var _binding: FragmentMatchingBinding? = null
    private val binding get() = _binding!!

    private lateinit var jobListAdapter: JobListAdapter
    private val jobList = listOf(
        JobItem("50K Sign-on Bonus | Apply Now! CSR-Voice | Start Monday | HMO + Fixed weekends off!", "✅iPloy, OPC Inc.", "Customer Service"),
        JobItem("Senior Software Engineer | Full-time | Remote work available | Competitive salary", "🚀Tech Innovations Ltd.", "Technology"),
        JobItem("Marketing Manager | Part-time | Flexible hours | Bonus opportunities", "🎯Digital Marketing Solutions", "Marketing"),
        JobItem("Customer Support Specialist | Contract | 24/7 shifts | Health benefits", "💼Supportive Solutions Inc.", "Customer Service"),
        JobItem("Data Analyst | Internship | Paid | 3 months | Immediate start", "📊Data Insights Co.", "Data Analysis"),
        JobItem("Project Manager | Full-time | Remote | Attractive package", "🏢Enterprise Solutions", "Management"),
        JobItem("Graphic Designer | Part-time | Freelance | Flexible schedule", "🎨Creative Minds Agency", "Design"),
        JobItem("Sales Executive | Full-time | Commission-based | Growth opportunities", "📈Sales Pros Inc.", "Sales"),
        JobItem("HR Specialist | Full-time | On-site | Benefits included", "👥People First HR", "Human Resources"),
        JobItem("Content Writer | Freelance | Remote | Competitive pay", "✍️Content Creators Ltd.", "Writing"),
        JobItem("Operations Manager | Full-time | In-office | Health benefits", "🏭Industrial Experts", "Operations"),
        JobItem("Network Engineer | Full-time | Remote | Stock options", "🔌Net Solutions Inc.", "IT"),
        JobItem("Product Manager | Full-time | Remote | Equity options", "🛍️Retail Innovations", "Product Management"),
        JobItem("UX/UI Designer | Freelance | Remote | Flexible hours", "💻Design Hub", "Design"),
        JobItem("Financial Analyst | Full-time | On-site | Annual bonuses", "💵Finance Experts", "Finance"),
        JobItem("Administrative Assistant | Part-time | Flexible schedule | Paid training", "🏢Office Works", "Administration"),
        JobItem("Software Tester | Full-time | Remote | Performance bonuses", "🐞Bug Hunters Inc.", "Technology"),
        JobItem("Business Analyst | Full-time | On-site | Career development", "📊Business Insights", "Business Analysis"),
        JobItem("Social Media Manager | Part-time | Remote | Competitive salary", "📱Social Media Gurus", "Marketing")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val matchingViewModel =
            ViewModelProvider(this).get(MatchingViewModel::class.java)

        _binding = FragmentMatchingBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView: RecyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        jobListAdapter = JobListAdapter(jobList, this)
        recyclerView.adapter = jobListAdapter

        val searchEditText: EditText = binding.searchTab
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                jobListAdapter.filter.filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return root
    }

    override fun onItemClick(item: JobItem) {
        val intent = Intent(activity, JobDetails::class.java).apply {
            putExtra("jobDesc", item.jobDesc)
            putExtra("jobCompany", item.jobCompany)
            putExtra("jobCategory", item.jobCategory)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
