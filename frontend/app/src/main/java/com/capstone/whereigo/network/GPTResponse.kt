// network/GPTResponse.kt
package com.capstone.whereigo.network

data class GPTResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: GPTMessage
)
