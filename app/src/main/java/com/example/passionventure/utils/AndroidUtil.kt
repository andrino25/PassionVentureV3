package com.example.passionventure.utils

import User
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.passionventure.model.UserModel

class AndroidUtil {

    companion object {


        fun passUserModelAsIntent(intent: Intent, model: UserModel) {
            intent.putExtra("username", model.username)
            intent.putExtra("contact", model.contact)
            //intent.putExtra("fcmToken", model.fcmToken)
        }

        fun getUserModelFromIntent(intent: Intent): UserModel {
            val userModel = UserModel()
            userModel.username = intent.getStringExtra("username")
            userModel.contact = intent.getStringExtra("contact")
            //userModel.fcmToken = intent.getStringExtra("fcmToken")
            return userModel
        }

    }
}