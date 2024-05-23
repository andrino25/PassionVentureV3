package com.example.passionventure.model

import com.google.firebase.Timestamp

data class ChatroomModel(
    var chatroomId: String = "",
    var userIds: List<String> = mutableListOf(),
    var lastMessageTimestamp: Long = 0L,
    var lastMessageSenderId: String = "",
    var lastMessage: String = ""
)