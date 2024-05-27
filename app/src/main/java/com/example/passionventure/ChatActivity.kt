package com.example.passionventure

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Context
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passionventure.adapter.ChatRecyclerAdapter
import com.example.passionventure.model.ChatMessageModel
import com.example.passionventure.model.ChatroomModel
import com.example.passionventure.model.UserModel
import com.example.passionventure.utils.AndroidUtil
import com.example.passionventure.utils.FirebaseUtil
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private var otherUser: UserModel? = null
    private var chatroomId: String? = null
    private var chatroomModel: ChatroomModel? = null
    private var adapter: ChatRecyclerAdapter? = null

    private lateinit var messageInput: EditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var backBtn: ImageButton
    private lateinit var otherUsername: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Retrieve UserModel from intent
        otherUser = AndroidUtil.getUserModelFromIntent(intent)

        // Initialize views
        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)

        // Set other user's username
        otherUsername.text = otherUser?.username

        // Get the current username
        val currentUsername = FirebaseUtil.getCurrentUsername(this)
        if (currentUsername != null && otherUser != null) {
            // Get chatroom ID using usernames
            chatroomId = otherUser!!.username?.let {
                FirebaseUtil.getChatroomId(currentUsername, it)
            }

            // Create or get chatroom model
            getOrCreateChatroomModel(currentUsername)

            // Setup RecyclerView
            setupChatRecyclerView()
        } else {
            Toast.makeText(this, "Failed to retrieve user information", Toast.LENGTH_SHORT).show()
            finish()
        }

        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupChatRecyclerView() {
        chatroomId?.let { id ->
            val query = FirebaseDatabase.getInstance().getReference("chatrooms")
                .child(id)
                .child("chats")
                .orderByChild("timestamp")

            val options = FirebaseRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query, ChatMessageModel::class.java)
                .build()

            adapter = ChatRecyclerAdapter(options, applicationContext)
            val layoutManager = LinearLayoutManager(this)
            layoutManager.stackFromEnd = true
            recyclerView.layoutManager = layoutManager
            recyclerView.adapter = adapter
            adapter?.startListening()

            adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)
                    layoutManager.smoothScrollToPosition(recyclerView, null, adapter?.itemCount ?: 0)
                }
            })
        }
    }

    private fun sendMessageToUser(message: String) {
        val currentUsername = FirebaseUtil.getCurrentUsername(this)
        if (currentUsername != null && chatroomId != null) {
            val timestamp = System.currentTimeMillis()
            val chatMessageModel = ChatMessageModel(message, currentUsername, timestamp)

            val chatMessageRef = FirebaseUtil.getChatroomMessageReference(chatroomId!!).push()
            chatMessageRef.setValue(chatMessageModel).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    messageInput.setText("")
                    updateChatroomInfo(currentUsername, message, timestamp)
                } else {
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateChatroomInfo(senderId: String, message: String, timestamp: Long) {
        chatroomId?.let { chatroomId ->
            val chatroomRef = FirebaseUtil.getChatroomReference(chatroomId)
            chatroomRef.child("lastMessageSenderId").setValue(senderId)
            chatroomRef.child("lastMessage").setValue(message)
            chatroomRef.child("lastMessageTimestamp").setValue(timestamp)
        }
    }

    private fun getOrCreateChatroomModel(currentUsername: String) {
        chatroomId?.let { chatroomId ->
            val chatroomRef = FirebaseUtil.getChatroomReference(chatroomId)
            chatroomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val userIds = listOf(currentUsername, otherUser?.username ?: "")
                        chatroomModel = ChatroomModel(chatroomId, userIds, System.currentTimeMillis(), "")
                        chatroomRef.setValue(chatroomModel)
                    } else {
                        chatroomModel = snapshot.getValue(ChatroomModel::class.java)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
    }

}