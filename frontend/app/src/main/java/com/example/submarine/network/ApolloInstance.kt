package com.example.submarine.network

import com.apollographql.apollo3.ApolloClient


private const val GRAPHQL_URL = "http://10.0.2.2:4000/graphql"

// On crée une instance unique du client Apollo qui sera réutilisée
// dans toute l'application.
val apolloClient = ApolloClient.Builder()
    .serverUrl(GRAPHQL_URL)
    .build()
