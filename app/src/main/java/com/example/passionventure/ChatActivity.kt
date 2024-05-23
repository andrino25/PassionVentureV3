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
    private var currentUsername: String? = null

    private lateinit var messageInput: EditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var backBtn: ImageButton
    private lateinit var otherUsername: TextView
    private lateinit var recyclerView: RecyclerView

    companion object {
        private const val PREFERENCE_FILE_KEY = "com.example.passionventure.PREFERENCE_FILE_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        currentUsername = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            .getString("currentUsername", null)

        // Get chatroom ID using usernames
        chatroomId = currentUsername?.let { FirebaseUtil.getChatroomId(it, otherUser?.username ?: "") }

        initializeViews()
        otherUsername.text = otherUser?.username

        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        backBtn.setOnClickListener {
            onBackPressed()
        }

        getOrCreateChatroomModel()
    }

    private fun initializeViews() {
        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backBtn = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)
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
        chatroomModel?.apply {
            lastMessageTimestamp = System.currentTimeMillis()
            lastMessageSenderId = FirebaseUtil.getCurrentUsername(this@ChatActivity).toString()
            lastMessage = message
            FirebaseUtil.getChatroomReference(chatroomId).setValue(this)
        }

        val chatMessageModel = FirebaseUtil.getCurrentUsername(this)?.let {
            ChatMessageModel(message, it, System.currentTimeMillis())
        }
        if (chatMessageModel != null) {
            val chatMessageRef = FirebaseUtil.getChatroomMessageReference(chatroomId!!).push()
            chatMessageRef.setValue(chatMessageModel).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    messageInput.setText("")
                } else {
                    // Handle the error
                }
            }
        }
    }



    private fun getOrCreateChatroomModel() {
        chatroomId?.let { id ->
            val chatroomRef = FirebaseUtil.getChatroomReference(id)
            chatroomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // First time chat
                        val userIds = listOfNotNull(FirebaseUtil.getCurrentUsername(this@ChatActivity), otherUser?.username)
                        val newChatroomModel = ChatroomModel(id, userIds, System.currentTimeMillis(), "")
                        chatroomRef.setValue(newChatroomModel)
                    }
                    setupChatRecyclerView()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Failed to load chat room", Toast.LENGTH_SHORT).show()
                }
            })
        }
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