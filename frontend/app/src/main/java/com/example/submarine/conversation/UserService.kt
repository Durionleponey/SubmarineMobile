package com.example.submarine.conversation

import android.util.Log
import com.example.submarine.graphql.GetMyIdQuery // L'import de la classe générée
import com.example.submarine.network.Apollo

object UserService {
    private const val TAG = "UserService"

    suspend fun getMyId(): String? {
        Log.d(TAG, "Tentative de récupération de l'ID de l'utilisateur actuel...")
        try {
            val response = Apollo.apolloClient
                .query(GetMyIdQuery()) // On exécute la requête
                .execute()

            if (response.hasErrors()) {
                Log.e(TAG, "Erreur GraphQL en récupérant l'ID: ${response.errors}")
                return null
            }

            val myId = response.data?.me?._id
            if (myId != null) {
                Log.i(TAG, "ID de l'utilisateur actuel récupéré avec succès: $myId")
                return myId
            } else {
                Log.w(TAG, "La requête 'me' n'a retourné aucun ID.")
                return null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception lors de la récupération de l'ID", e)
            return null
        }
    }
}
