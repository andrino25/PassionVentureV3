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
    private val chatrooms: List<ChatroomModel>,
    private val context: Context,
    private val currentUserId: String
) : RecyclerView.Adapter<RecentChatRecyclerAdapter.ChatroomModelViewHolder>() {

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        var lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        var lastMessageTimeText: TextView = itemView.findViewById(R.id.last_message_time_text)



            // Add click listener to the item view

        fun bind(model: ChatroomModel) {
            lastMessageText.text = if (model.lastMessageSenderId == model.userIds.get(0)) {
                "You: ${model.lastMessage}"
            } else {
                model.lastMessage
            }
            itemView.setOnClickListener {
                // navigate to chat activity
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("username", model.userIds.get(1))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }

            usernameText.text = model.userIds.get(1)
            val formattedTime = FirebaseUtil.timestampToString(model.lastMessageTimestamp)
            lastMessageTimeText.text = formattedTime


            FirebaseUtil.getOtherUserFromChatroom(model.userIds, currentUserId) { otherUserModel ->
                usernameText.text = otherUserModel.username
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int) {
        holder.bind(chatrooms[position])
    }

    override fun getItemCount(): Int {
        return chatrooms.size
    }
}
