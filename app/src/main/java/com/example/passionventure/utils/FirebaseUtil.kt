package com.example.passionventure.utils

import android.content.Context
import android.util.Log
import com.example.passionventure.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseUtil {
    companion object {
        private const val USERS_PATH = "users"
        private const val CHATROOMS_PATH = "chatrooms"
        private const val PREFERENCE_FILE_KEY = "com.example.passionventure.PREFERENCE_FILE_KEY"
        private const val CURRENT_USERNAME_KEY = "currentUsername"


        // This function is replaced by a parameterized approach
        fun currentUserDetails(): DatabaseReference {
            return FirebaseDatabase.getInstance().reference.child(USERS_PATH)
        }

        // Get a reference to the users node
        fun allUserCollectionReference(): DatabaseReference {
            return FirebaseDatabase.getInstance().reference.child(USERS_PATH)
        }

        fun getChatroomReference(chatroomId: String): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId)
        }

        // Get a reference to the messages in a specific chatroom
        fun getChatroomMessageReference(chatroomId: String): DatabaseReference {
            return getChatroomReference(chatroomId).child("chats")
        }


        // Generate a chat room ID based on two user IDs
        fun getChatroomId(userId1: String, userId2: String): String {
            return if (userId1 < userId2) {
                "$userId1-$userId2"
            } else {
                "$userId2-$userId1"
            }
        }


        // Get a reference to the chatrooms node
        fun allChatroomCollectionReference(): DatabaseReference {
            return FirebaseDatabase.getInstance().reference.child(CHATROOMS_PATH)
        }

        // Get the other user from a chatroom
        fun getOtherUserFromChatroom(context: Context, userIds: List<String>, listener: ValueEventListener) {
            val currentUserId = getCurrentUsername(context)
            val otherUserId = userIds.find { it != currentUserId }
            if (otherUserId != null) {
                Log.d("FirebaseUtil", "Current User ID: $currentUserId, Other User ID: $otherUserId")
                allUserCollectionReference().child(otherUserId).addListenerForSingleValueEvent(listener)
            } else {
                Log.e("FirebaseUtil", "Other user ID not found in userIds: $userIds")
            }
        }

        fun getCurrentUsername(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            return sharedPref.getString(CURRENT_USERNAME_KEY, null)
        }

        // Convert a timestamp to a formatted string
        fun timestampToString(timestamp: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }

        fun getUserByUsername(username: String, listener: ValueEventListener) {
            val usersRef = FirebaseDatabase.getInstance().getReference("users")
            val query = usersRef.orderByChild("username").equalTo(username)
            query.addListenerForSingleValueEvent(listener)
        }

    }
}