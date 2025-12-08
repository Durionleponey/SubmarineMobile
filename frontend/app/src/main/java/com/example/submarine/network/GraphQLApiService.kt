package com.example.submarine.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// 🧩 Requête GraphQL générique
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any?>? = null
)

// 🧭 Réponse GraphQL générique
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<Map<String, Any>>? = null
)

// 🔌 Interface Retrofit pour toutes les requêtes GraphQL
interface GraphQLApiService {

    // ⭐ Méthode générique d'origine (utilisée par signup, bio, contacts, etc.)
    @POST("graphql")
    suspend fun <T> executeGraphQL(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<T>>

    // ⭐ Méthode dédiée : mutation sendAdminThanks
    @POST("graphql")
    suspend fun sendAdminThanksMutation(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<SendAdminThanksData>>

    // ⭐ Méthode dédiée : mutation sendAlertMessage
    @POST("graphql")
    suspend fun sendAlertMessageMutation(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<SendAlertMessageData>>
}
