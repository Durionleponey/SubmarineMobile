package com.example.submarine.conversation

import android.util.Log
import com.apollographql.apollo3.api.Optional
import com.example.submarine.graphql.CreateTestGroupMutation
import com.example.submarine.network.Apollo
import com.example.submarine.graphql.CreateMessageMutation
import com.example.submarine.graphql.type.CreateMessageInput
import com.example.submarine.graphql.GetMessagesQuery
import com.apollographql.apollo3.exception.ApolloException
import com.example.submarine.graphql.GetMyIdQuery
import com.example.submarine.graphql.GetMyConversationsQuery
import com.example.submarine.graphql.GetUserPublicKeyQuery
import com.example.submarine.network.TokenProvider
import com.example.submarine.graphql.UpdateMyPublicKeyMutation

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


    suspend fun sendMessage(
        content: String,
        chatId: String
    ): Result<CreateMessageMutation.CreateMessage?> {
        Log.d(TAG, "Tentative de sendMessage avec le contenu: $content et le chatId: $chatId")
        try {
            val createMessageInput = CreateMessageInput(
                content = content,
                chatId = chatId
            )

            val response = Apollo.apolloClient
                .mutation(CreateMessageMutation(createMessageInput))
                .execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString { it.message } ?: "Unknown error"
                Log.e(TAG, "Erreur GraphQL lors de la création du message: $errorMessage")
                return Result.failure(Exception("Erreur GraphQL: $errorMessage"))
            }

            val sentMessage = response.data?.createMessage
            if (sentMessage != null) {
                Log.i(TAG, "Message envoyé avec succès. ID: ${sentMessage._id}")
                return Result.success(sentMessage)
            }else {
                Log.w(
                    TAG,
                    "La création du message n'a retourné aucune donnée, mais pas d'erreur GraphQL."
                )
                return Result.failure(Exception("Les données du message créé sont nulles."))
            }

        }catch (e: Exception) {
            Log.e(TAG, "Exception lors de la création du message", e)
            return Result.failure(e)
        }

    }

    /**
     * Fonction pour récupérer les messages d'une conv entamée et existante
     *
     * @param chatId L'ID du chat pour lequel on souhaite récupérer les messages.
     * @return Un Result contenant la liste des messages récupérés ou une exception.
     */

    suspend fun getMessages(chatId: String): Result<List<GetMessagesQuery.GetMessage>> {
        Log.d(TAG, "Tentative de récupérer les messages pour le chatId: $chatId")
        try {
            val response = Apollo.apolloClient
                .query(GetMessagesQuery(chatId = chatId))
                .execute()

            if (response.hasErrors()) {
                val errorMessage = response.errors?.joinToString { it.message } ?: "Unknown error"
                Log.e(TAG, "Erreur GraphQL lors de la récupération des messages: $errorMessage")
                return Result.failure(Exception("Erreur GraphQL: $errorMessage"))
            }

            val messages = response.data?.getMessages
            if(messages != null){
                Log.i(TAG, "Récupération des messages réussie. Nombre de messages: ${messages.size}")
                return Result.success(messages)
            }else{
                Log.i(TAG, "Aucune donnée de message trouvée ${chatId}")
                return Result.success(emptyList())

            }

        }catch(e: ApolloException){
            Log.e(TAG, "Exception lors de la récupération des messages", e)
            return Result.failure(e)
        }
    }
    // Dans ConversationMaker.kt

    suspend fun getAllMyChats(): Result<List<GetMyConversationsQuery.Chatss>> {
        Log.d(TAG, "Récupération de toutes les conversations via chatss...")
        try {
            // On appelle la query qu'on vient de créer
            val response = Apollo.apolloClient.query(GetMyConversationsQuery()).execute()

            if (response.hasErrors()) {
                Log.e(TAG, "Erreur GraphQL: ${response.errors}")
                return Result.failure(Exception("Erreur GraphQL"))
            }

            // Attention : Apollo reprend le nom du champ, donc ici .chatss
            val chats = response.data?.chatss ?: emptyList()

            return Result.success(chats)

        } catch (e: Exception) {
            Log.e(TAG, "Exception récupération chats", e)
            return Result.failure(e)
        }
    }
}

object UserService {

    suspend fun getMyId(): String? {
        try {
            Log.d("Apollo", "Récupération de mon ID...")
            val response = Apollo.apolloClient.query(GetMyIdQuery()).execute()

            if (response.hasErrors()) {
                Log.e("Apollo", "Erreur GetMyId: ${response.errors}")
                return null
            }

            return response.data?.me?._id
        } catch (e: Exception) {
            Log.e("Apollo", "Exception GetMyId", e)
            return null
        }
    }

    suspend fun getPublicKey(userId: String): String? {
        try {
            val response = Apollo.apolloClient
                .query(GetUserPublicKeyQuery(userId = userId))
                .execute()

            return response.data?.user?.publicKey
        } catch (e: Exception) {
            Log.e("Crypto", "Erreur récupération clé publique", e)
            return null
        }
    }
    // ConversationMaker.kt (Ajouter à l'objet UserService)

    suspend fun sendPublicKey(publicKey: String): Result<Boolean> {
        try {
            // NOTE: Ceci est un PSEUDO-CODE. Vous devez créer la classe de mutation
            //       (ex: UpdatePublicKeyMutation) si elle n'existe pas.
            val response = Apollo.apolloClient
                .mutation(UpdateMyPublicKeyMutation(publicKey = publicKey))
                .execute()

            if (response.hasErrors()) {
                Log.e("Crypto", "Erreur GraphQL lors de l'envoi de la clé: ${response.errors}")
                return Result.failure(Exception("Échec de la mutation"))
            }

            Log.i("Crypto", "Clé publique envoyée au serveur avec succès.")
            return Result.success(true)

        } catch (e: Exception) {
            Log.e("Crypto", "Exception lors de l'envoi de la clé", e)
            return Result.failure(e)
        }
    }


}

