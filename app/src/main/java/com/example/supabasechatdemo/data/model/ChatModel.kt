package com.example.supabasechatdemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatModel(
    val id: Int? = null,
    val created_at: String = "",
    val message: String = "",
    val sender_id: String = "",
    val receiver_id: String = "",
    val date: String = ""
)