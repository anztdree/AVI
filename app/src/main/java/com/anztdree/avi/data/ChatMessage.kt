package com.anztdree.avi.data

import kotlinx.serialization.Serializable

/**
 * Satu pesan dalam percakapan dengan AVI.
 * role: "user" atau "avi"
 */
@Serializable
data class ChatMessage(
    val role: String,
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
