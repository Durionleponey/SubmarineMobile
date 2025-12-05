package com.example.submarine.listeContact.API

import com.apollographql.apollo3.ApolloClient

object GraphQLClient {
    val client = ApolloClient.Builder()
        .serverUrl("http://10.0.2.2:4000/graphql") // ou ton domaine
        .build()
}