package com.example.submarine.conversation

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.example.submarine.graphql.CreateTestGroupMutation
import com.example.submarine.network.Apollo

object ChatService {
    private const val TAG = "ChatService"

    /**
     * Crée une nouvelle conversation (chat).
     * @param userIds La liste des IDs des utilisateurs à inclure.
     * @param isPrivate Vrai si c'est une conversation privée (généralement entre 2 personnes).
     * @param name Le nom du groupe (optionnel, principalement pour les conversations de groupe).
     * @return Un Result contenant le chat créé ou une exception.
     */
    suspend fun createChat(
        userIds: List<String>,
        isPrivate: Boolean,
        name: String? = null
    ): Result<CreateTestGroupMutation.CreateChat?> {
        Log.d(
            TAG,
            "Tentative de création de chat avec les utilisateurs: $userIds. Privée: $isPrivate"
        )

        Log.d(TAG, "Paramètre userIds reçu: $userIds")
        Log.d(TAG, "Paramètre isPrivate reçu: $isPrivate")
        Log.d(TAG, "Paramètre name reçu: $name")



        try {
                val chatName = Optional.presentIfNotNull(name)
                val userIdsOptional = Optional.present(userIds)

                val response = Apollo.apolloClient
                    .mutation(
                        CreateTestGroupMutation(
                            userIds = userIdsOptional,
                            isPrivate = isPrivate,
                            name = chatName
                        )
                    ).execute()

                if (response.hasErrors()) {
                    Log.e(TAG, "Erreur GraphQL lors de la création du chat: ${response.errors}")
                    return Result.failure(Exception("Erreur GraphQL: ${response.errors?.firstOrNull()?.message}"))
                }

                val createdChat = response.data?.createChat
                if (createdChat != null) {
                    Log.i(TAG, "Chat créé avec succès. ID: ${createdChat._id}")
                    return Result.success(createdChat)
                } else {
                    Log.w(
                        TAG,
                        "La création du chat n'a retourné aucune donnée, mais pas d'erreur GraphQL."
                    )
                    return Result.failure(Exception("Les données du chat créé sont nulles."))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception lors de la création du chat", e)
                return Result.failure(e)
            }
        }
    }

