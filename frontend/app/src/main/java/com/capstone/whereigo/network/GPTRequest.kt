// network/GPTRequest.kt
package com.capstone.whereigo.network

data class GPTMessage(
    val role: String,
    val content: String
)

data class GPTRequest(
    val model: String = "gpt-4o-mini",
    val messages: List<GPTMessage>
)
