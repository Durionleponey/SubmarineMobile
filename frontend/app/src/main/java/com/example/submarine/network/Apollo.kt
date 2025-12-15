package com.example.submarine.network

import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.apollographql.apollo3.network.http.HttpNetworkTransport
import com.apollographql.apollo3.network.ws.GraphQLWsProtocol
import com.apollographql.apollo3.network.ws.SubscriptionWsProtocol
import com.apollographql.apollo3.network.ws.WebSocketNetworkTransport
import com.example.submarine.BuildConfig

private const val APOLLO_TAG = "Apollo"
private class AuthorizationInterceptor : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        val token = TokenProvider.token

        if (token == null) {
            Log.w(APOLLO_TAG, "Interception http, token non trouvé")
        }else{
            Log.d(APOLLO_TAG, "Interception http, token trouvé")
        }

        val newRequest = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(newRequest)
    }
}

object Apollo {
    val apolloClient: ApolloClient by lazy {
        Log.d(APOLLO_TAG, "Creating Apollo Client")

        val servHttp = "http://${BuildConfig.SERVER_IP}:4000/graphql"
        val servWebSocket = "ws://${BuildConfig.SERVER_IP}:4000/graphql"

        Log.d(APOLLO_TAG, "URL HTTP $servHttp")
        Log.d(APOLLO_TAG, "URL WS $servWebSocket")

        val httpNetworkTransport = HttpNetworkTransport.Builder()
            .serverUrl(servHttp)
            .addInterceptor(AuthorizationInterceptor())
            .build()



        val webSocketNetworkTransport = WebSocketNetworkTransport.Builder()
            .serverUrl(servWebSocket)
            .protocol(
                protocolFactory = SubscriptionWsProtocol.Factory(
                    connectionPayload = {
                        val token =TokenProvider.token
                        Log.d(APOLLO_TAG, "[WS PAYLOAD] - Le token récupéré est: '$token'")
                        if(token != null && token.isNotEmpty()){
                            val  payload = mapOf(
                                "Authorization" to "Bearer $token"
                            )
                            Log.d(APOLLO_TAG, "Interception WS, token trouvé ${payload}")
                            payload
                        }else{
                            Log.w(APOLLO_TAG, "Interception WS, token non trouvé")
                            emptyMap()
                        }
                    }
                )
            )
            .build()
        //niveau chifrement , couches
        //RGPD
        //cadre local
        /**      email: "marie@example.com"
        password: "StrongPass123!"*/


        ApolloClient.Builder()
            .networkTransport(httpNetworkTransport)
            .subscriptionNetworkTransport(webSocketNetworkTransport)
            .build()
    }
}
