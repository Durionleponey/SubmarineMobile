package com.example.submarine.network

import com.apollographql.apollo3.ApolloClient

object Apollo {
    val apolloClient: ApolloClient by lazy {
        println("Creating Apollo Client")
        ApolloClient.Builder()
            .serverUrl("http://10.0.2.2:4000/graphql")
            .build()

    }
}