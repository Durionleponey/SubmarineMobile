package com.example.submarine.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)
data class UserResponse(val _id: String, val email: String)

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("auth/profile")
    suspend fun getProfile(): Response<UserResponse>

    // 👇 C'est la nouvelle ligne à ajouter
    @POST("auth/logout")
    suspend fun logout(): Response<Void>
}