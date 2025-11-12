package com.example.submarine.listeContact

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class UserDto(
    val _id: String,
    val pseudo: String
)

interface UserApiService {
    @GET("users/search")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("pseudo") pseudo: String
    ): List<UserDto>
}

object UserApi {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000") // ⚠️ adapte ton URL (avec /api/ si global prefix)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(UserApiService::class.java)

    suspend fun searchUsers(token: String, pseudo: String): List<String> {
        val users = service.searchUsers("Bearer $token", pseudo)
        return users.map { it.pseudo }
    }
}
