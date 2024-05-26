package com.example.passionventure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.adapter.RecentChatRecyclerAdapter
import com.example.passionventure.model.ChatroomModel
import com.example.passionventure.utils.FirebaseUtil
import com.firebase.ui.database.FirebaseRecyclerOptions

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private var adapter: RecentChatRecyclerAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        val query = FirebaseUtil.allChatroomCollectionReference()
            .orderByChild("lastMessageTimestamp")

        val options = FirebaseRecyclerOptions.Builder<ChatroomModel>()
            .setQuery(query, ChatroomModel::class.java)
            .build()

        adapter = RecentChatRecyclerAdapter(options, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }
}
