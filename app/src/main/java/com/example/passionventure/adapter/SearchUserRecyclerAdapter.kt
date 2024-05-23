package com.example.passionventure.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.ChatActivity
import com.example.passionventure.R
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.AndroidUtil
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

class SearchUserRecyclerAdapter(
    options: FirebaseRecyclerOptions<UserModel>,
    private val context: Context,
    private val currentUsername: String?
) : FirebaseRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder>(options) {

    inner class UserModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        private val phoneText: TextView = itemView.findViewById(R.id.phone_text)
        private val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)

        fun bind(model: UserModel) {
            usernameText.text = if (model.username == currentUsername) {
                "${model.username} (Me)"
            } else {
                model.username
            }
            phoneText.text = model.contact


            itemView.setOnClickListener {
                // navigate to chat activity
                val intent = Intent(context, ChatActivity::class.java)
                AndroidUtil.passUserModelAsIntent(intent, model)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_user_recycler_row, parent, false)
        return UserModelViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserModelViewHolder, position: Int, model: UserModel) {
        holder.bind(model)
    }
}