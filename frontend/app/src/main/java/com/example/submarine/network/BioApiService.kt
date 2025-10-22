package com.example.submarine.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// Requête GraphQL
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>? = null
)

// Réponse GraphQL générique
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<Map<String, Any>>?
)

// Structure spécifique à la mutation updateBio
data class UpdateBioData(
    val updateBio: UserData
)

data class UserData(
    val _id: String,
    val pseudo: String,
    val bio: String?
)

interface BioApiService {

    // 🧩 Mutation pour mettre à jour la bio
    @POST("graphql")
    suspend fun updateBio(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<UpdateBioData>>

    // 🧭 Query générique (ex: "me")
    @POST("graphql")
    suspend fun queryGraphQL(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<Map<String, Any>>>
}
