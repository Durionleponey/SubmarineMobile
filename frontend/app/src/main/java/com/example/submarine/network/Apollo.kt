package com.example.submarine.network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.http.HttpRequest
import com.apollographql.apollo.api.http.HttpResponse
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain
import com.apollographql.apollo.network.http.HttpNetworkTransport
import com.apollographql.apollo.network.ws.WebSocketNetworkTransport
import com.example.submarine.BuildConfig
import com.apollographql.apollo.network.ws.GraphQLWsProtocol

private const val APOLLO_TAG = "Apollo"

/**
 * Intercepteur pour ajouter le token d'authentification aux requêtes HTTP (Query/Mutation).
 * Le token est ajouté dans l'en-tête "Authorization: Bearer <token>".
 */
private class AuthorizationInterceptor : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        val token = TokenProvider.token

        if (token == null) {
            Log.w(APOLLO_TAG, "Interception http, token non trouvé")
        } else {
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

        // 1. Définition des URLs
        // L'URL HTTP pour les requêtes (Query/Mutation)
        val servHttp = "http://${BuildConfig.SERVER_IP}:4000/graphql"
        // L'URL WebSocket pour les souscriptions (Subscription)
        val servWebSocket = "ws://${BuildConfig.SERVER_IP}:4000/graphql"
        // NOTE: Utiliser 'ws://' pour le développement local si vous n'utilisez pas HTTPS/WSS.

        Log.d(APOLLO_TAG, "URL HTTP: $servHttp")
        Log.d(APOLLO_TAG, "URL WS: $servWebSocket")


        // 2. Transport HTTP (pour Query et Mutation)
        val httpNetworkTransport = HttpNetworkTransport.Builder()
            .serverUrl(servHttp)
            .addInterceptor(AuthorizationInterceptor()) // Ajoute le token à l'en-tête HTTP
            .build()


        // 3. Transport WebSocket (pour Subscription)
        val webSocketNetworkTransport = WebSocketNetworkTransport.Builder()
            .serverUrl(servWebSocket) // <-- CORRECTION CLÉ : Utilisation de la bonne variable d'URL
            .protocol(
                GraphQLWsProtocol.Factory(
                    connectionPayload = {
                        val token = TokenProvider.token
                        // Le token est envoyé AU MOMENT de la connexion WebSocket
                        if (!token.isNullOrEmpty()) {
                            mapOf("authorization" to "Bearer $token").also {
                                Log.d("APOLLO_WS", "Token envoyé via WS: $it")
                            }
                        } else {
                            // Si le token est null, le serveur risque de refuser la connexion immédiatement
                            Log.w("APOLLO_WS", "Aucun token trouvé lors de la connexion WS. La connexion pourrait échouer.")
                            emptyMap()
                        }
                    }
                )
            )
            .build()

        // 4. Création du client Apollo
        ApolloClient.Builder()
            .networkTransport(httpNetworkTransport) // Utilise le transport HTTP
            .subscriptionNetworkTransport(webSocketNetworkTransport) // Utilise le transport WS pour les souscriptions
            .build()

    }
}