package com.example.submarine.listeContact.API

import com.example.submarine.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// DTO pour les réponses JSON
data class UserDto(
    val _id: String,
    val pseudo: String,
    val bio: String? = null
)

// Définition des routes API REST
interface UserApiService {
    @GET("users/search") // Attention: vérifie que cette route existe sur ton backend (ex: http://ip:4000/users/search)
    suspend fun searchUsers(
        @Header("Authorization") token: String,
        @Query("pseudo") pseudo: String
    ): List<UserDto>

    @GET("users/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): UserDto
}

object UserApi {
    // 1. Configurer les logs pour voir les requêtes
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // 2. Construire l'URL REST (Pas GraphQL !)
    // Note: On enlève "/graphql" car c'est du REST ici
    private const val BASE_URL = "http://${BuildConfig.SERVER_IP}:4000/"

    // 3. Créer l'instance Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 4. Créer le service
    private val service = retrofit.create(UserApiService::class.java)

    // 5. Fonctions publiques
    suspend fun searchUsers(token: String, pseudo: String): List<UserDto> {
        return service.searchUsers("Bearer $token", pseudo)
    }

    suspend fun getMe(token: String): UserDto {
        return service.getMe("Bearer $token")
    }
}