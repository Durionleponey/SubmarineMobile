package com.example.submarine.conversation.composants

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submarine.conversation.ChatService
import com.example.submarine.conversation.Subscribe
import com.example.submarine.conversation.UserPseudoRecup
import com.example.submarine.graphql.GetMessagesQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlin.collections.emptyList


sealed class ChatState {
    object Idle : ChatState()
    object Creating : ChatState()
    data class CreationSuccess(val chatId: String) : ChatState()
    data class Error(val errMessages: String) : ChatState()
}

sealed class SubscriptionState{
    object Disconnected : SubscriptionState()
    object Connecting : SubscriptionState()
    object Connected : SubscriptionState()
    data class Error(val messages: String) : SubscriptionState()
}

class ConversationViewModel : ViewModel() {

    private val TAG = "ConversationViewModel"

    private val _creationState = MutableStateFlow<ChatState>(ChatState.Idle)
    val creationState = _creationState.asStateFlow()

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Disconnected)
    val subscriptionState = _subscriptionState.asStateFlow()

    private val _messages = MutableStateFlow<List<GetMessagesQuery.GetMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private var subscriptionJob: Job? = null

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()


    /**
     * Crée le chat puis s'abonne à ses messages
     *
     * @param userId

     */

    fun createChat(userIds: List<String>, isPrivate: Boolean, name: String? = null) {
        if (_creationState.value is com.example.submarine.conversation.ChatState.Creating) {
            Log.w(TAG, "createChat() called while already creating a chat")
            return
        }


        viewModelScope.launch {
            _creationState.value = ChatState.Creating
            val result = ChatService.createChat(userIds, isPrivate, name)

            result.onSuccess { chat ->
                val chatId = chat?._id
                if (chatId != null) {
                    Log.i(TAG, "Chat created with ID: $chatId")
                    _activeChatId.value = chatId
                    _creationState.value = ChatState.CreationSuccess(chatId)
                    loadMessages(chatId)
                    subscribeToChat(chatId)

                } else {
                    Log.e(TAG, "Chat creation failed")
                    _creationState.value = ChatState.Error("Chat creation failed")
                }
            }.onFailure { e ->
                Log.e(TAG, "Chat creation failed", e)
                _creationState.value = ChatState.Error("Chat creation failed: ${e.message}")
            }
        }
    }

    /**
     * S'abonne à un chat
     *
     * @param chatId
     */
    private fun subscribeToChat(chatId: String ) {
        subscriptionJob?.cancel()

        _subscriptionState.value = SubscriptionState.Connecting

        subscriptionJob = viewModelScope.launch {
            Subscribe.subscribeToConversation(chatId)
                .collect { newMessageFromSubscription ->
                    if(_subscriptionState.value !is com.example.submarine.conversation.SubscriptionState.Connected) {
                        _subscriptionState.value = SubscriptionState.Connected
                        Log.i(TAG, "Subscribed to chat with ID: $chatId")
                    }
                    val convertedMessage = GetMessagesQuery.GetMessage(
                        _id = newMessageFromSubscription._id,
                        content = newMessageFromSubscription.content,
                        userId = newMessageFromSubscription.userId,
                    )

                    _messages.update { currentMessages -> currentMessages + convertedMessage }
                }
        }
    }

    /**
     * Fonction appelée par le UI
     */

    fun sendMessage(messageContent: String) {
        val chatId = _activeChatId.value
        if (chatId == null) {
            Log.w(TAG, "sendMessage() called with no active chat")
            return
        }

        if (messageContent.isBlank()) {
            Log.w(TAG, "sendMessage() called with empty message")
            return
        }

        viewModelScope.launch {
            val result = ChatService.sendMessage(messageContent, chatId)
            result.onSuccess { sentMessage ->
                Log.i(TAG, "Message sent: $sentMessage")
            }.onFailure { e ->
                Log.e(TAG, "Message sending failed", e)
            }
        }
    }

    /**
     * Nettoie la routine lors de la fermeture du ViewModel
     * pour evoiter les fuites de memoire
     */

    override fun onCleared() {
        Log.d(TAG, "onCleared() called. Annule la subscription.")
        subscriptionJob?.cancel()
        super.onCleared()
    }


    private val userPseudoRecup = UserPseudoRecup()
    private val _userPseudo = MutableStateFlow<String>("char")
    val userPseudo = _userPseudo.asStateFlow()


    /**
     * lance la recup du ID , le view model !!!
     *
     * @param userId
     */

    fun chargePseudo(userId: String) {
        Log.d(TAG, "chargePseudo() called with: userId = $userId")
        viewModelScope.launch {
            val userPseudo = userPseudoRecup.fetchUser(userId)

            Log.d("ConversationViewModel", "userPseudo = $userPseudo")
            _userPseudo.value = userPseudo ?: "User inconnu"



        }
    }

    /**
     * Fonction pour afficher l'historique des messages du chat
     * @param chatId id du chat
     */

    private fun loadMessages(chatId: String ) {
        Log.d(TAG, "loadMessages() called")
        viewModelScope.launch {
            val result = ChatService.getMessages(chatId)
            result.onSuccess { messagesHist ->
                Log.i(TAG, "Messages loaded: $messagesHist")
                _messages.value = messagesHist
            }.onFailure { e ->
                Log.e(TAG, "Message loading failed", e)
                _creationState.value = ChatState.Error("Message loading failed: ${e.message}")

            }
        }

    }



}

