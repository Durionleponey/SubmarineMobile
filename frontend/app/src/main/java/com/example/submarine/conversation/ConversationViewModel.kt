package com.example.submarine.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submarine.conversation.ChatState
import com.example.submarine.conversation.SubscriptionState
import com.example.submarine.conversation.UserService.sendPublicKey
import com.example.submarine.graphql.GetMessagesQuery
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

private const val ENCRYPTION_ENABLED = true
class ConversationViewModel : ViewModel() {

    private val TAG = "ConversationViewModel"

    /**private val _creationState = MutableStateFlow<ChatState>(ChatState.Idle)
    val creationState = _creationState.asStateFlow()
    */

    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.Disconnected)
    val subscriptionState = _subscriptionState.asStateFlow()




    private val _currentUserState = MutableStateFlow<String?>(null)
    val currentUserState = _currentUserState.asStateFlow()
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


    private val _isEncryptionEnabled = MutableStateFlow(true)
    val isEncryptionEnabled = _isEncryptionEnabled.asStateFlow()
    /**
     * POINT D'ENTRÉE : Appelé par l'UI à l'ouverture de l'écran
     */



    fun createOrGetChat(userIds: List<String>, contactId: String, isPrivate: Boolean = true, name: String? = null) {
        viewModelScope.launch {

            chargePseudo(contactId)
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
        _subscriptionState.value = SubscriptionState.Connecting

        subscriptionJob = viewModelScope.launch {
            Log.d(TAG, "Abonnement WS au chat: $chatId")

            Subscribe.subscribeToConversation(chatId)
                .catch { e ->
                    Log.e(TAG, "Erreur dans l'abonnement WS", e)
                    _subscriptionState.value = SubscriptionState.Error(e.message ?: "Erreur inconnue")
                }
                .collect { newMessage ->

                    if (_subscriptionState.value !is SubscriptionState.Connected) {
                        _subscriptionState.value = SubscriptionState.Connected
                    }

                    Log.d(TAG, "WS Message reçu: ${newMessage.content}")

                    // 1. Conversion du message de la subscription en objet local
                    val convertedMessage = GetMessagesQuery.GetMessage(
                        _id = newMessage._id,
                        content = newMessage.content, // Contient le texte chiffré/brut
                        userId = newMessage.userId,
                    )

                    val contentToDisplay = if (convertedMessage.userId != myUserId) {
                        // CAS A : Message REÇU (de l'autre) -> DOIT ÊTRE DÉCHIFFRÉ
                        if (CryptoManager.isEncrypted(convertedMessage.content)) {
                            try {
                                CryptoManager.decrypt(convertedMessage.content) //
                            } catch (e: Exception) {
                                Log.e(TAG, "Decryption error for received WS message", e)
                                "Message illisible (WS)"
                            }
                        } else {
                            // Message non chiffré reçu de l'autre (mode test)
                            convertedMessage.content
                        }
                    } else {
                        // CAS B : Message ENVOYÉ (par moi) venant du serveur
                        // On essaie de récupérer la version en clair qu'on a stockée localement lors de l'envoi
                        LocalMessageStore.getPlaintext(convertedMessage._id) ?: //
                        "Message chiffré (Base locale manquante pour mon message)"
                    }


                    // 3. Création de l'objet message à afficher dans l'UI
                    val messageForUI = convertedMessage.copy(content = contentToDisplay)

                    _messages.update { currentList ->
                        if (currentList.any { it._id == messageForUI._id }) {
                            currentList
                        } else {
                            currentList + messageForUI
                        }
                    }
                }
        }
    }


    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            val result = ChatService.getMessages(chatId)

            result.onSuccess { messagesHist ->

// 1. TRAITEMENT : On déchiffre les messages reçus
                val processedMessages = messagesHist.map { msg ->

                    val contentToDisplay = if (msg.userId != myUserId) {
                        // CAS A : Message reçu (de l'autre) -> DOIT ÊTRE DÉCHIFFRÉ (si le format est bon)
                        if (CryptoManager.isEncrypted(msg.content)) {
                            try {
                                CryptoManager.decrypt(msg.content)
                            } catch (e: Exception) {
                                Log.e(TAG, "Decryption error for received message", e)
                                "Message illisible"
                            }
                        } else {
                            // Message non chiffré reçu de l'autre (probablement en mode test)
                            msg.content
                        }
                    } else {
                        // CAS B : Message envoyé (par moi) venant du serveur

                        val localPlaintext = LocalMessageStore.getPlaintext(msg._id)

                        if (localPlaintext != null) {
                            localPlaintext
                        } else if (CryptoManager.isEncrypted(msg.content)) {
                            "Message chiffré (Base locale manquante)"
                        } else {
                            msg.content
                        }
                    }
                    msg.copy(content = contentToDisplay)
                }
                _messages.update { currentList ->
                    val currentIds = currentList.map { it._id }.toSet()

                    val newMessages = processedMessages.filter { it._id !in currentIds }
                    currentList + newMessages
                }

            }.onFailure { e ->
                Log.e(TAG, "Echec chargement historique", e)
            }
        }
    }


    fun sendMessage(messageContent: String, senderId: String, contactId: String) {
        val chatId = _activeChatId.value

        if (chatId != null && messageContent.isNotBlank()) {
            viewModelScope.launch {

                var contentToSend = messageContent

                // --- ÉTAPE 1 : CRYPTOGRAPHIE ---
                if (_isEncryptionEnabled.value){

                    val recipientPublicKeyStr = UserService.getPublicKey(contactId)

                    if (recipientPublicKeyStr == null) {
                        Log.e(TAG, "ERREUR : Impossible de trouver la clé publique pour $contactId. Envoi annulé.")
                        return@launch
                    }

                    try {
                        val recipientKey = CryptoManager.stringToPublicKey(recipientPublicKeyStr)
                        contentToSend = CryptoManager.encrypt(messageContent, recipientKey)
                    } catch (e: Exception) {
                        Log.e(TAG, "ERREUR : Echec lors du chiffrement du message", e)
                        return@launch
                    }
                    Log.d(TAG, "Message chiffré: $contentToSend")
                } else {
                    Log.w(TAG, "Mode non chiffré activé. Message non chiffré, envoyé en clair : $contentToSend")

                }

                val result = ChatService.sendMessage(contentToSend, chatId)

                result.onSuccess { sentMessage ->
                    Log.i(TAG, "Message chiffré envoyé avec succès au serveur")

                    if (sentMessage != null) {

                        // Ceci est CRUCIAL pour la relecture de l'historique E2EE.
                        LocalMessageStore.storePlaintext(
                            messageId = sentMessage._id,
                            plaintext = messageContent // messageContent est le texte en clair
                        )

                        // --- ÉTAPE 3 : MISE À JOUR UI LOCALE ---

                        val finalUserId = sentMessage.userId ?: senderId


                        val newMessageForUI = GetMessagesQuery.GetMessage(
                            _id = sentMessage._id,
                            content = messageContent, // <--- ICI : On force l'affichage en clair
                            userId = finalUserId
                        )

                        _messages.update { currentList ->
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
            Log.w(TAG, "Tentative d'envoi échouée. ChatID: $chatId")
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
    fun loadCurrentUser() {
        viewModelScope.launch {
            val myId = UserService.getMyId()

            if (myId != null) {
                myUserId = myId
                _currentUserState.value = myId
                Log.d(TAG, "Utilisateur identifié avec succès : $myId")

                val myPseudo = userPseudoRecup.fetchUser(myId)
                Log.d(TAG, "Pseudo de l'utilisateur courant : $myPseudo")
            } else {
                Log.e(TAG, "Impossible d'identifier l'utilisateur courant (getMyId a retourné null)")
            }



            myUserId = myId
            _currentUserState.value = myId
           // Log.w(TAG, " ID UTILISATEUR FORCÉ POUR TEST : $hardcodedId")
        }
    }

    fun toggleEncryption(enabled: Boolean) {
        _isEncryptionEnabled.value = enabled
        Log.d(TAG, "Chiffrement activé : $enabled")
    }


}