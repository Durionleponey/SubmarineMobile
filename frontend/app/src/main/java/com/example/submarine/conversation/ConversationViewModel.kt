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
    private var myUserId: String? = null

    // Pour gérer les pseudos
    private val userPseudoRecup = UserPseudoRecup()
    private val _userPseudo = MutableStateFlow<String>("Chargement...")
    val userPseudo = _userPseudo.asStateFlow()

    /**
     * POINT D'ENTRÉE : Appelé par l'UI à l'ouverture de l'écran
     */
    fun initConversation(contactId: String) {
        viewModelScope.launch {
            if (myUserId == null) {
                val fetchedId = UserService.getMyId()
                if (fetchedId == null) {
                  //  _creationState.value = ChatState.Error("Impossible de récupérer votre profil utilisateur.")
                    return@launch
                }
                myUserId = fetchedId
                Log.i(TAG, "Mon ID est : $myUserId")
            }

            chargePseudo(contactId)

            val participants = listOf(myUserId!!, contactId)
            createOrGetChat(participants, isPrivate = true)
        }
    }

    fun createOrGetChat(userIds: List<String>, isPrivate: Boolean, name: String? = null) {
        //if (_creationState.value is ChatState.Creating) return

        viewModelScope.launch {
           // _creationState.value = ChatState.Creating

            val result = ChatService.createChat(userIds, isPrivate, name)

            result.onSuccess { chat ->
                val chatId = chat?._id
                if (chatId != null) {
                    Log.i(TAG, "Chat actif ID: $chatId")

                    _activeChatId.value = chatId
                    //_creationState.value = ChatState.CreationSuccess(chatId)

                    loadMessages(chatId)
                    subscribeToChat(chatId)

                } else {
                    //_creationState.value = ChatState.Error("Erreur: Chat ID is null")
                }
            }.onFailure { e ->
                Log.e(TAG, "Erreur création/récupération chat", e)
                //_creationState.value = ChatState.Error(e.message ?: "Erreur inconnue")
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
                _messages.value = messagesHist
            }.onFailure { e ->
                Log.e(TAG, "Echec chargement historique", e)
            }
        }
    }

    fun sendMessage(messageContent: String) {
        val chatId = _activeChatId.value
        if (chatId != null && messageContent.isNotBlank()) {
            viewModelScope.launch {

                val result = ChatService.sendMessage(messageContent, chatId)
                result.onSuccess {
                    Log.i(TAG, "Message envoyé avec succès")
                }.onFailure { e ->
                    Log.e(TAG, "Echec envoi message", e)

                }
            }
        } else {
            Log.w(TAG, "Tentative d'envoi sans ChatID ou message vide")
        }
    }
    fun chargePseudo(userId: String) {
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