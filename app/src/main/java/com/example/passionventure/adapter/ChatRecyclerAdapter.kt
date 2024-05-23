package com.example.passionventure.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.databinding.ChatMessageRecyclerRowBinding
import com.example.passionventure.model.ChatMessageModel
import com.example.passionventure.utils.FirebaseUtil
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class ChatRecyclerAdapter(
    options: FirebaseRecyclerOptions<ChatMessageModel>,
    private val context: Context
) : FirebaseRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatModelViewHolder>(options) {

    override fun onBindViewHolder(holder: ChatModelViewHolder, position: Int, model: ChatMessageModel) {
        val currentUsername = FirebaseUtil.getCurrentUsername(context)
        val isCurrentUser = currentUsername != null && model.senderId == currentUsername

        if (isCurrentUser) {
            holder.binding.leftChatLayout.visibility = View.GONE
            holder.binding.rightChatLayout.visibility = View.VISIBLE
            holder.binding.rightChatTextview.text = model.message
        } else {
            holder.binding.rightChatLayout.visibility = View.GONE
            holder.binding.leftChatLayout.visibility = View.VISIBLE
            holder.binding.leftChatTextview.text = model.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatModelViewHolder {
        val binding = ChatMessageRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatModelViewHolder(binding)
    }

    inner class ChatModelViewHolder(val binding: ChatMessageRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)
}

