package com.example.submarine.conversation


import com.apollographql.apollo3.exception.ApolloException
import com.example.submarine.graphql.GetUserByIDQuery
import com.example.submarine.network.Apollo

/**
 * Récupération du pseudo de l'utilisateur
 *
 * @param userID identifiant de l'utilisateur
 * @return pseudo de l'utilisateur String
 */

class UserPseudoRecup{

    suspend fun fetchUser(userId: String): String? {
        try {
            val response = Apollo.apolloClient
                .query(GetUserByIDQuery(userId = userId))
                .execute()

            return response.data?.user?.pseudo
        } catch (e: ApolloException) {
            println("erreur recuperation pseudo")
            return null

        }

    }

}