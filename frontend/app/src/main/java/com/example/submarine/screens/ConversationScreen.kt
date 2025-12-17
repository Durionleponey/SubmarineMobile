package com.example.submarine.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.conversation.SubscriptionState
import com.example.submarine.conversation.composants.MessageBubble
import com.example.submarine.conversation.composants.IconUser
import com.example.submarine.conversation.composants.Media
import com.example.submarine.conversation.composants.PhotoSend
import com.example.submarine.conversation.composants.SendButton
import com.example.submarine.graphql.GetMessagesQuery


data class Message(
    val text: String,
    val isSentByMe: Boolean
)

@OptIn(ExperimentalMaterial3Api::class) // Ajout pour l'utilisation de TopAppBar et Scaffold
@Composable
fun ConversationScreen(
    contactName: String,
    messages: List<GetMessagesQuery.GetMessage>,
    onNavigateBack: () -> Unit,
    currentUserId: String,
    onSentMessage: (String) -> Unit,
    isEncryptionEnabled: Boolean,
    onToggleEncryption: (Boolean) -> Unit,
    subscriptionState: SubscriptionState
) {
    var textState by remember { mutableStateOf("") }

    // Utilisation de Scaffold pour une structure d'écran standard et une gestion facile des insets
    Scaffold(
        // Barre supérieure qui contiendra l'icône de retour, le nom et le switch de chiffrement
        topBar = {
            TopAppBar(
                title = {
                    // Conteneur pour le nom et le switch
                    Column(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        // Ligne pour l'icône utilisateur et le nom
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconUser(modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier
                                .width(16.dp))
                            Text(
                                text = contactName,
                                style = MaterialTheme.typography.titleLarge
                            )

                        }

                        // Ligne pour le switch de chiffrement, placée juste en dessous
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                text = if (isEncryptionEnabled) "CHIFFREMENT ON" else "CHIFFREMENT OFF",
                                color = if (isEncryptionEnabled) Color.Blue else MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.width(10.dp))
                            Switch(
                                checked = isEncryptionEnabled,
                                onCheckedChange = onToggleEncryption // Maintenant, cette variable existe !
                            )
                        }


                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        // Barre inférieure pour l'envoi de messages
        bottomBar = {
            MessageInputBar(
                textState = textState,
                onTextChange = { textState = it },
                onSentMessage = {
                    onSentMessage(textState)
                    textState = ""
                }
            )
        },
        containerColor = Color.Gray.copy(alpha = 0.1f)
    ) { innerPadding ->
        // Le contenu principal (la liste des messages)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                // IMPORTANT : Appliquer le padding fourni par Scaffold
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            reverseLayout = true // Pour afficher les derniers messages en bas
        ) {
            items(messages.asReversed()) { message ->
                val isSentByMe = message.userId == currentUserId
                MessageBubble(message = message.content, isSentByMe = isSentByMe)
            }
        }
    }
}


// Composant réutilisable pour la barre d'envoi de message
@Composable
private fun MessageInputBar(
    textState: String,
    onTextChange: (String) -> Unit,
    onSentMessage: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // Modifier pour éviter d'être recouvert par la barre de navigation
            .navigationBarsPadding(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Media(modifier = Modifier.size(40.dp))
            PhotoSend(modifier = Modifier.size(40.dp))

            OutlinedTextField(
                value = textState,
                onValueChange = onTextChange,
                placeholder = { Text("Écrire un message...") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            SendButton(
                isEnabled = textState.isNotBlank(),
                onClick = onSentMessage
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ConversationScreenPreview() {
    // 1. Préparer des données factices pour l'aperçu

    // 2. Appeler le composable ConversationScreen avec les données
    ConversationScreen(
        contactName = "Alex",
        messages = emptyList(),
        onNavigateBack = {},
        currentUserId = "currentUserId",
        onSentMessage = {},
        isEncryptionEnabled = true,
        onToggleEncryption = {},
        subscriptionState = SubscriptionState.Connected // ou .Connected, .Error
    )
}

@Preview(showBackground = true, name = "Chiffrement OFF")
@Composable
fun ConversationScreenEncryptionOffPreview() {
    ConversationScreen(
        contactName = "Alex",
        messages = emptyList(),
        onNavigateBack = {},
        currentUserId = "currentUserId",
        onSentMessage = {},
        isEncryptionEnabled = false,
        onToggleEncryption = {},
        subscriptionState = SubscriptionState.Connected
    )
}

