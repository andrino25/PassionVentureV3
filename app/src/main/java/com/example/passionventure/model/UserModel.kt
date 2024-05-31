package com.example.passionventure.model

import com.google.firebase.Timestamp


data class UserModel(
    var contact: String? = null,
    var username: String? = null,
    var createdTimestamp: Timestamp? = null,
    var userId: String? = null,
    val isCurrentUser: Boolean = false,
    var fcmToken: String? = null,
    var profilePicUrl: String = ""
)

