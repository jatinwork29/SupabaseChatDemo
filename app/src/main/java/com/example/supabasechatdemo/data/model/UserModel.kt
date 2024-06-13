package com.example.supabasechatdemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: Int? = null,
    val user_email: String = "",
    val user_password: String = "",
    val user_fcm: String? = null,
    val user_uid: String = "",
)
