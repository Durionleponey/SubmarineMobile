package com.example.submarine.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// 🧩 Requête GraphQL générique
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, Any>? = null
)

// 🧭 Réponse GraphQL générique
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<Map<String, Any>>?
)



// 🔌 Interface Retrofit pour toutes les requêtes GraphQL
interface GraphQLApiService {

    // ⚙️ Envoie une mutation ou requête GraphQL avec typage spécifique
    @POST("graphql")
    suspend fun <T> executeGraphQL(
        @Header("Authorization") token: String,
        @Body request: GraphQLRequest
    ): Response<GraphQLResponse<T>>

}
