// network/GPTApiService.kt
package com.capstone.whereigo.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface GPTApiService {
    @POST("/gpt")
    suspend fun sendMessage(@Body body: GPTRequest): Response<GPTResponse>
}
