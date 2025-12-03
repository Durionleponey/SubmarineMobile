package com.example.submarine.listeContact.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// J'ai ajouté 'bio' pour que l'interface puisse l'afficher
data class UserDto(
    val _id: String,
    val pseudo: String,
    val bio: String? = null // Peut être null si pas défini
)

interface UserApiService {
    // Recherche d'autres utilisateurs
    @GET("users/search")
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("pseudo") pseudo: String
    ): List<UserDto>

    // 🔥 NOUVEAU : Récupérer MON profil
    // Assure-toi que ton backend a une route GET /users/me ou /users/profile
    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): UserDto
}

object UserApi {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:4000/") // Attention au slash à la fin
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(UserApiService::class.java)

    suspend fun searchUsers(token: String, pseudo: String): List<UserDto> {
        return service.searchUsers("Bearer $token", pseudo)
    }

    // 🔥 Fonction à appeler dans ContactsActivity
    suspend fun getMe(token: String): UserDto {
        return service.getMe("Bearer $token")
    }
}