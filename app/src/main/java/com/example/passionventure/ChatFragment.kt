package com.example.passionventure

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.adapter.RecentChatRecyclerAdapter
import com.example.passionventure.model.ChatroomModel
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.AndroidUtil
import com.example.passionventure.utils.FirebaseUtil
import com.google.firebase.database.*

class ChatFragment : Fragment(), RecentChatRecyclerAdapter.OnChatItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter

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
        val currentUsername = FirebaseUtil.getCurrentUsername(requireContext())
        val chatRoomsRef = FirebaseUtil.allChatroomCollectionReference()

        chatRoomsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRooms = mutableListOf<ChatroomModel>()
                for (roomSnapshot in snapshot.children) {
                    val room = roomSnapshot.getValue(ChatroomModel::class.java)
                    if (room != null && room.userIds.contains(currentUsername)) {
                        chatRooms.add(room)
                    }
                }
                adapter = RecentChatRecyclerAdapter(chatRooms.sortedByDescending { it.lastMessageTimestamp }, requireContext(), this@ChatFragment)
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    override fun onChatItemClick(chatRoom: ChatroomModel) {
        val currentUsername = FirebaseUtil.getCurrentUsername(requireContext())
        val otherUsername = chatRoom.userIds.find { it != currentUsername }
        if (otherUsername != null) {
            FirebaseUtil.getUserByUsername(otherUsername, object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val otherUserModel = snapshot.getValue(UserModel::class.java)
                    if (otherUserModel != null) {
                        val intent = Intent(requireContext(), ChatActivity::class.java)
                        AndroidUtil.passUserModelAsIntent(intent, otherUserModel) // Pass the user data
                        intent.putExtra("chatRoomId", chatRoom.chatroomId)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
