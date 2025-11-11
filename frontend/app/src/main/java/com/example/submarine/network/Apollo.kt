package com.example.submarine.network

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.HttpNetworkTransport
import com.apollographql.apollo3.network.ws.WebSocketNetworkTransport
import com.apollographql.apollo3.network.http.DefaultHttpEngine

object Apollo {
    val apolloClient: ApolloClient by lazy {
        Log.d("Apollo", "Creating Apollo Client")


        val servHttp = "http://10.0.2.2:4000/graphql"
        val servWebSocket = "ws://10.0.2.2:4000/graphql"
        ApolloClient.Builder()
            .serverUrl(servHttp)
            .subscriptionNetworkTransport(
                WebSocketNetworkTransport.Builder()
                    .serverUrl(servWebSocket)
                    .build()
            )

            .build()

    }
}