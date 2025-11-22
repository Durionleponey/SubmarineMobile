package com.example.submarine.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.submarine.graphql.SubscribeToMessagesSubscription
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update


sealed class ChatState {
    object Idle : ChatState()
    object Creating : ChatState()
    data class CreationSuccess(val chatId: String) : ChatState()
    data class Error(val message: String) : ChatState()
}

sealed class SubscriptionState{
    object Disconnected : SubscriptionState()
    object Connecting : SubscriptionState()
    object Connected : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

class ConversationViewModel : ViewModel() {

    private val TAG = "ConversationViewModel"

    private val _creationState = MutableStateFlow<ChatState>(ChatState.Idle)
    val creationState = _creationState.asStateFlow()

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Disconnected)
    val subscriptionState = _subscriptionState.asStateFlow()

    private val _messages = MutableStateFlow<List<SubscribeToMessagesSubscription.MessageCreated>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _currentId = MutableStateFlow<String?>(null)
    val currentUserId = _currentId.asStateFlow()

    private var subscriptionJob: Job? = null


    init {
        viewModelScope.launch {
            Log.d(TAG, "Bloc init: Lancement de la récupération de l'ID de l'utilisateur actuel...")
            val myId = UserService.getMyId()
            if (myId != null) {
                _currentId.value = myId
                Log.i(TAG, "ID de l'utilisateur actuel initialisé: $myId")
            } else {
                Log.e(TAG, "Impossible de récupérer l'ID de l'utilisateur actuel.")
            }
        }
    }

    /**
     * Crée le chat puis s'abonne à ses messages
     *
     * @param userId

     */

    fun createChat(userIds: List<String>, isPrivate: Boolean, name: String? = null) {
        if (_creationState.value is ChatState.Creating) {
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
                    _creationState.value = ChatState.CreationSuccess(chatId)
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
    private fun subscribeToChat(chatId: String) {
        subscriptionJob?.cancel()
        _messages.value = emptyList()

        _subscriptionState.value = SubscriptionState.Connecting

        subscriptionJob = viewModelScope.launch {
            Subscribe.subscribeToConversation(chatId)
                .collect { newMessages ->
                    if(_subscriptionState.value !is SubscriptionState.Connected) {
                        _subscriptionState.value = SubscriptionState.Connected
                        Log.i(TAG, "Subscribed to chat with ID: $chatId")
                    }
                    _messages.update { currentMessages ->
                        currentMessages + newMessages
                    }
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


}

