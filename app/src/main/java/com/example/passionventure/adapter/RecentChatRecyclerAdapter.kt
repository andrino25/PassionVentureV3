package com.example.passionventure.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ChatActivity
import com.example.passionventure.R
import com.example.passionventure.model.ChatroomModel
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.AndroidUtil
import com.example.passionventure.utils.FirebaseUtil
import com.google.firebase.database.*

class RecentChatRecyclerAdapter(
    private var chatRooms: List<ChatroomModel>,
    private val context: Context,
    private val listener: OnChatItemClickListener
) : RecyclerView.Adapter<RecentChatRecyclerAdapter.ChatroomModelViewHolder>() {

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.user_name_text)
        private val lastMessageTextView: TextView = itemView.findViewById(R.id.last_message_text)
        private val timestampTextView: TextView = itemView.findViewById(R.id.last_message_time_text)

        fun bind(chatRoom: ChatroomModel) {
            FirebaseUtil.getOtherUserFromChatroom(context, chatRoom.userIds, object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val otherUserModel = snapshot.getValue(UserModel::class.java)
                    if (otherUserModel != null && !otherUserModel.username.isNullOrEmpty()) {
                        usernameTextView.text = otherUserModel.username
                    } else {
                        usernameTextView.text = chatRoom.userIds.find { it != FirebaseUtil.getCurrentUsername(context) }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    usernameTextView.text = chatRoom.userIds.find { it != FirebaseUtil.getCurrentUsername(context) }
                }
            })

            lastMessageTextView.text = if (chatRoom.lastMessageSenderId == FirebaseUtil.getCurrentUsername(context)) {
                "You: ${chatRoom.lastMessage}"
            } else {
                chatRoom.lastMessage
            }
            timestampTextView.text = FirebaseUtil.timestampToString(chatRoom.lastMessageTimestamp)

            itemView.setOnClickListener {
                listener.onChatItemClick(chatRoom)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)
    }

    override fun getItemCount(): Int {
        return chatRooms.size
    }

    interface OnChatItemClickListener {
        fun onChatItemClick(chatRoom: ChatroomModel)
    }
}