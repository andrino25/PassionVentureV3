package com.example.passionventure.utils

import android.content.Context
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

        fun getOtherUserFromChatroom(userIds: List<String>, currentUserId: String, callback: (UserModel) -> Unit) {
            val otherUserId = userIds.firstOrNull { it != currentUserId }

            if (otherUserId != null) {
                val userReference = FirebaseDatabase.getInstance().getReference("users").child(otherUserId)
                userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val otherUserModel = dataSnapshot.getValue(UserModel::class.java)
                        if (otherUserModel != null) {
                            callback(otherUserModel)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle possible errors.
                    }
                })
            }
        }

        fun getChatroomReference(chatroomId: String?): DatabaseReference {
            return FirebaseDatabase.getInstance().getReference("chatrooms").child(chatroomId!!)
        }

        fun getChatroomMessageReference(chatroomId: String?): DatabaseReference {
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

        // Save the current username to shared preferences
        fun saveCurrentUsername(context: Context, username: String) {
            val sharedPref = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(CURRENT_USERNAME_KEY, username)
                apply()
            }
        }

        // Retrieve the current username from shared preferences
        fun getCurrentUsername(context: Context): String? {
            val sharedPref = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
            return sharedPref.getString(CURRENT_USERNAME_KEY, null)
        }

        fun allChatroomCollectionReference(): DatabaseReference {
            return FirebaseDatabase.getInstance().reference.child(CHATROOMS_PATH)
        }

        // Get the other user from a chatroom
        fun getOtherUserFromChatroom(context: Context, userIds: List<String>, listener: ValueEventListener) {
            val currentUserId = getCurrentUsername(context)
            val otherUserId = userIds.first { it != currentUserId }
            FirebaseDatabase.getInstance().reference.child(USERS_PATH).child(otherUserId)
                .addListenerForSingleValueEvent(listener)
        }

        // Convert a timestamp to a formatted string
        fun timestampToString(timestamp: Long): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        }


//        // Get the current user's profile picture storage reference
//        fun getCurrentProfilePicStorageRef(context: Context): StorageReference {
//            val currentUsername = getCurrentUsername(context)
//            return FirebaseStorage.getInstance().reference.child("profile_pic")
//                .child(currentUsername ?: throw IllegalStateException("User is not logged in"))
//        }
//
//        // Get the other user's profile picture storage reference
//        fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
//            return FirebaseStorage.getInstance().reference.child("profile_pic")
//                .child(otherUserId)
//        }




    }
}