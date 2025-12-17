package com.example.submarine.conversation


import android.util.Log
import com.example.submarine.graphql.GetUserByIDQuery
import com.example.submarine.network.Apollo
import com.apollographql.apollo.exception.ApolloException


/**
 * Récupération du pseudo de l'utilisateur
 *
 * @param userID identifiant de l'utilisateur
 * @return pseudo de l'utilisateur String
 */

class UserPseudoRecup{

    suspend fun fetchUser(userId: String): String? {
        Log.d("UserPseudoRecup", "fetchUser: $userId")
        try {
            val response = Apollo.apolloClient
                .query(GetUserByIDQuery(userId = userId))
                .execute()
            if (response.hasErrors()) {
                Log.d("UserPseudoRecup", "fetchUser: ${response.errors}")
                return null
            }
            val pesudo = response.data?.user?.pseudo
            Log.d("UserPseudoRecup", "fetchUser: $pesudo")

            return pesudo
        } catch (e: ApolloException) {

            Log.d("UserPseudoRecup", "fetchUser ECHEC: ${e.message}")
            return null

        }

    }

}