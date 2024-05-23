package com.example.passionventure.model

import com.google.firebase.Timestamp

data class ChatMessageModel(
    var message: String = "",
    var senderId: String = "",
    var timestamp: Long = 0L
)