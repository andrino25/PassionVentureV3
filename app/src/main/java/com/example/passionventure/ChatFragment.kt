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
import com.google.firebase.database.*

class ChatFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentUserId: String
    private lateinit var adapter: RecentChatRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)

        // Retrieve the current user ID from arguments
        currentUserId = arguments?.getString(ARG_CURRENT_USER_ID) ?: ""

        setupRecyclerView()
        return view
    }

    private fun setupRecyclerView() {
        val query = FirebaseUtil.allChatroomCollectionReference()
            .orderByChild("lastMessageTimestamp")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chatrooms = mutableListOf<ChatroomModel>()
                for (chatroomSnapshot in dataSnapshot.children) {
                    val chatroomModel = chatroomSnapshot.getValue(ChatroomModel::class.java)
                    if (chatroomModel?.userIds?.get(0)?.contains(currentUserId) == true) {
                        chatrooms.add(chatroomModel)
                    }
                }
                setupAdapter(chatrooms)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setupAdapter(chatrooms: List<ChatroomModel>) {
        adapter = RecentChatRecyclerAdapter(chatrooms, requireContext(), currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    companion object {
        private const val ARG_CURRENT_USER_ID = "currentUserId"

        fun newInstance(currentUserId: String): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString(ARG_CURRENT_USER_ID, currentUserId)
            fragment.arguments = args
            return fragment
        }
    }
}
