package com.example.submarine.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submarine.conversation.ChatState
import com.example.submarine.conversation.SubscriptionState
import com.example.submarine.graphql.GetMessagesQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.emptyList



sealed class ChatState {
    object Idle : ChatState()
    object Creating : ChatState()
    data class CreationSuccess(val chatId: String) : ChatState()
    data class Error(val errMessages: String) : ChatState()
}

sealed class SubscriptionState {
    object Disconnected : SubscriptionState()
    object Connecting : SubscriptionState()
    object Connected : SubscriptionState()
    data class Error(val messages: String) : SubscriptionState()
}

class ConversationViewModel : ViewModel() {

    private val TAG = "ConversationViewModel"

    /**private val _creationState = MutableStateFlow<ChatState>(ChatState.Idle)
    val creationState = _creationState.asStateFlow()

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Disconnected)
    val subscriptionState = _subscriptionState.asStateFlow()
    */
    private val _messages = MutableStateFlow<List<GetMessagesQuery.GetMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    private var subscriptionJob: Job? = null
    var myUserId: String? = null

    // Pour gérer les pseudos
    private val userPseudoRecup = UserPseudoRecup()
    private val _userPseudo = MutableStateFlow<String>("Chargement...")
    val userPseudo = _userPseudo.asStateFlow()

    /**
     * POINT D'ENTRÉE : Appelé par l'UI à l'ouverture de l'écran
     */


// Dans ConversationViewModel.kt

    fun createOrGetChat(userIds: List<String>, isPrivate: Boolean, name: String? = null) {
        viewModelScope.launch {

            // 1. On récupère la liste des conversations existantes
            val resultList = ChatService.getAllMyChats()

            resultList.onSuccess { chatList ->
                // 2. On cherche si la conversation existe déjà
                val found = chatList.find { chat ->
                    val chatParticipants = chat.userIds

                    if (chatParticipants != null && chat.isPrivate == isPrivate) {
                        // On vérifie que la liste contient tous les participants et a la même taille
                        chatParticipants.containsAll(userIds) && chatParticipants.size == userIds.size
                    } else {
                        false
                    }
                }

                // 3. LOGIQUE QUI MANQUAIT : On agit selon le résultat
                if (found != null) {
                    // CAS A : ON A TROUVÉ LA CONVERSATION
                    Log.i(TAG, "Conversation existante trouvée ID: ${found._id}")
                    val chatId = found._id
                    _activeChatId.value = chatId
                    loadMessages(chatId)
                    subscribeToChat(chatId)
                } else {
                    // CAS B : ON N'A RIEN TROUVÉ -> ON CRÉE
                    Log.i(TAG, "Aucune conversation trouvée. Création d'une nouvelle...")

                    // Appel au backend pour créer
                    val creationResult = ChatService.createChat(userIds, isPrivate, name)

                    creationResult.onSuccess { createdChat ->
                        val newChatId = createdChat?._id
                        if (newChatId != null) {
                            Log.i(TAG, "Nouvelle conversation créée ID: $newChatId")
                            _activeChatId.value = newChatId
                            loadMessages(newChatId)
                            subscribeToChat(newChatId)
                        }
                    }.onFailure { e ->
                        Log.e(TAG, "Erreur lors de la création du chat", e)
                    }
                }
            }.onFailure { e ->
                Log.e(TAG, "Impossible de récupérer la liste des chats", e)
                // En cas d'échec de récupération, par sécurité, on peut tenter de créer
                // ou afficher une erreur. Ici on log juste l'erreur.
            }
        }
    }
    private fun subscribeToChat(chatId: String) {
        subscriptionJob?.cancel()
        //_subscriptionState.value = SubscriptionState.Connecting

        subscriptionJob = viewModelScope.launch {
            Log.d(TAG, "Abonnement WS au chat: $chatId")

            Subscribe.subscribeToConversation(chatId)
                .collect { newMessage ->
                   /** if (_subscriptionState.value !is SubscriptionState.Connected) {
                        _subscriptionState.value = SubscriptionState.Connected
                    }*/

                    Log.d(TAG, "WS Message reçu: ${newMessage.content}")

                    val convertedMessage = GetMessagesQuery.GetMessage(
                        _id = newMessage._id,
                        content = newMessage.content,
                        userId = newMessage.userId,
                    )

                    _messages.update { currentList ->
                        if (currentList.any { it._id == convertedMessage._id }) {
                            currentList
                        } else {
                            currentList + convertedMessage
                        }
                    }
                }
        }
    }


    private fun loadMessages(chatId: String) {
        viewModelScope.launch {
            val result = ChatService.getMessages(chatId)
            result.onSuccess { messagesHist ->
                _messages.update { currentList ->
                    val currentIds = currentList.map { it._id }.toSet()

                    val newMessages = messagesHist.filter { it._id !in currentIds }

                    currentList + newMessages
                }
            }.onFailure { e ->
                Log.e(TAG, "Echec chargement historique", e)
            }
        }
    }


// ConversationViewModel.kt

    // Ajoutez le paramètre senderId
// Dans ConversationViewModel.kt

    fun sendMessage(messageContent: String, senderId: String) {
        val chatId = _activeChatId.value

        if (chatId != null && messageContent.isNotBlank()) {
            viewModelScope.launch {

                val result = ChatService.sendMessage(messageContent, chatId)
                result.onSuccess { sentMessage ->
                    Log.i(TAG, "Message envoyé avec succès")

                    // CORRECTION : On ne redéfinit pas senderId avec myUserId.
                    // On utilise le 'senderId' passé en paramètre de la fonction si sentMessage.userId est null.

                    if (sentMessage != null) {

                        // On détermine l'ID à utiliser pour l'affichage
                        val finalUserId = sentMessage.userId ?: senderId

                        val newMessageForUI = GetMessagesQuery.GetMessage(
                            _id = sentMessage._id,
                            content = sentMessage.content,
                            userId = finalUserId
                        )

                        _messages.update { currentList ->
                            // On évite les doublons si le WebSocket a déjà reçu le message entre temps
                            if (currentList.any { it._id == newMessageForUI._id }) {
                                currentList
                            } else {
                                currentList + newMessageForUI
                            }
                        }
                    }
                }.onFailure { e ->
                    Log.e(TAG, "Echec envoi message", e)
                }
            }
        } else {
            Log.w(TAG, "Tentative d'envoi échouée. ChatID: $chatId, SenderID: $senderId")
        }
    }    fun chargePseudo(userId: String) {
        viewModelScope.launch {
            val pseudo = userPseudoRecup.fetchUser(userId)
            _userPseudo.value = pseudo ?: "Utilisateur inconnu"
        }
    }

    override fun onCleared() {
        Log.d(TAG, "ViewModel onCleared. Nettoyage.")
        subscriptionJob?.cancel()
        super.onCleared()
    }
}