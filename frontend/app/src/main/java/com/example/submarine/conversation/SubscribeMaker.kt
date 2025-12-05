package com.example.submarine.conversation

import android.util.Log
import com.apollographql.apollo3.exception.ApolloException
import com.example.submarine.graphql.GetMessagesQuery
import com.example.submarine.graphql.SubscribeToMessagesSubscription
import com.example.submarine.network.Apollo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

object Subscribe {
    private const val TAG = "Subscribe"

    /**
     * Souscrire à une conversation
     *
     * @param chatID identifiant de la conversation
     *@return Subscription
     */

    fun subscribeToConversation(chatID: String): Flow<SubscribeToMessagesSubscription.MessageCreated >{
        Log.d(TAG, "subscribeToConversation: $chatID")

        return Apollo.apolloClient
                .subscription(SubscribeToMessagesSubscription(chatID))
                .toFlow()
                .onStart {
                    Log.d(TAG, "subscribeToConversation: onStart début de la collecte du chat $chatID")
                }
                .onCompletion { cause ->
                    if (cause == null) {
                        Log.d(
                            TAG,
                            "subscribeToConversation: onCompletion fin de la collecte du chat $chatID"
                        )
                    }else {
                        Log.w(TAG, "subscribeToConversation: onCompletion $cause")
                    }
                }
                .catch { e ->
                    Log.d(TAG, "subscribeToConversation: $e")
                    if (e is ApolloException){
                        Log.d(TAG, "subscribeToConversation: $e")
                    }
                }
                .mapNotNull { rep ->
                        if (rep.hasErrors()) {
                            Log.d(TAG, "subscribeToConversation: ${rep.errors}")
                        }
                        rep.data?.messageCreated
                }




    }
}