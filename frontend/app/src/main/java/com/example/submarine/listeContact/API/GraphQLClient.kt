package com.example.submarine.listeContact.API

import com.apollographql.apollo3.ApolloClient
import com.example.submarine.BuildConfig

object GraphQLClient {
    // On construit l'URL complète ici
    private const val BASE_URL = "http://${BuildConfig.SERVER_IP}:4000/graphql"

    val client = ApolloClient.Builder()
        .serverUrl(BASE_URL)
        .build()
}