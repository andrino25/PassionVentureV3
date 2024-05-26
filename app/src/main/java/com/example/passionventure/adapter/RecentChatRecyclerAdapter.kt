package com.example.passionventure.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ChatActivity
import com.example.passionventure.R
import com.example.passionventure.model.ChatroomModel
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.AndroidUtil
import com.example.passionventure.utils.FirebaseUtil
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class RecentChatRecyclerAdapter(
    options: FirebaseRecyclerOptions<ChatroomModel>,
    private val context: Context
) : FirebaseRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(options) {

    inner class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        var lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        var lastMessageTimeText: TextView = itemView.findViewById(R.id.last_message_time_text)

        fun bind(model: ChatroomModel) {


            FirebaseUtil.getOtherUserFromChatroom(context, model.userIds, object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val otherUserModel = snapshot.getValue(UserModel::class.java)
                    otherUserModel?.let { userModel ->
                        val currentUserId = FirebaseUtil.getCurrentUsername(context)
                        val lastMessageSentByMe = model.lastMessageSenderId == currentUserId

                        usernameText.text = userModel.username
                        lastMessageText.text = if (lastMessageSentByMe) {
                            "You: ${model.lastMessage}"
                        } else {
                            model.lastMessage
                        }
                        lastMessageTimeText.text = model.lastMessageTimestamp.let { timestamp ->
                            FirebaseUtil.timestampToString(timestamp)
                        }

                        itemView.setOnClickListener {
                            val intent = Intent(context, ChatActivity::class.java)
                            AndroidUtil.passUserModelAsIntent(intent, userModel)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("RecentChatAdapter", "Failed to fetch user data", error.toException())
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int, model: ChatroomModel) {
        holder.bind(model)
    }
}
