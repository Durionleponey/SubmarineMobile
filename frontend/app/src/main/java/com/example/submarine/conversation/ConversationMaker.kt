package com.example.submarine.conversation

import android.util.Log
import com.example.submarine.graphql.CreateTestGroupMutation
import com.example.submarine.network.Apollo.apolloClient
import com.apollographql.apollo3.api.Optional

/**
 * creation d'une nouvelle session chat
 *
 * @param userIds liste des contacts dans la conv , peut etre juste un
 * @param isPrivate boolean pour dire si la conv est entre deux pers seulement
 *
 */

class ChatSessionStarter (userIds: List<String>, isPrivate: Boolean, name: String? = null) {

    suspend fun makeChat(userIds: List<String>?, isPrivate: Boolean, name: String? = null): Result<CreateTestGroupMutation.CreateChat?> {

        Log.d("makeChat", "makeChat avec: $userIds et privée $isPrivate")
        try {
            val response = apolloClient
                .mutation(CreateTestGroupMutation(

                    userIds = Optional.present(userIds),
                    isPrivate = isPrivate,
                    name = Optional.present(name)
                )
            ).execute()

            if (response.hasErrors()) {
                Log.d("makeChat", "makeChat hasErrors: ${response.errors}")
                return Result.failure(Exception("makeChat hasErrors: ${response.errors}"))

            }else {
                Log.i("makeChat", "makeChat success: ${response.data?.createChat?._id}")
                return Result.success(response.data?.createChat)
            }

        } catch (e: Exception) {
            Log.d("makeChat", "makeChat catch")
            return Result.failure(e)
        }

    }
}
