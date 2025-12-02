package com.example.submarine.conversation

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box // Import ajouté
import androidx.compose.foundation.layout.fillMaxSize // Import ajouté
import androidx.compose.material3.CircularProgressIndicator // Import ajouté
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment // Import ajouté
import androidx.compose.ui.Modifier // Import ajouté
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme

class ConversationActivity : ComponentActivity() {

    private val TAG = "ConversationActivity"
    private val viewModel: ConversationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val contactId = intent.getStringExtra("contactId")

        if (contactId == null){
            Log.e(TAG, "L'ID de l'utilisateur n'a pas été transmis.")
            finish()
            return
        } else {
            viewModel.chargePseudo(contactId)
        }

        setContent {
            SubmarineTheme {
                val pseudo by viewModel.userPseudo.collectAsState()
                val messages by viewModel.messages.collectAsState()
                val currentUserId by viewModel.currentUserState.collectAsState()

                // 1. On lance le chargement de "QUI SUIS-JE"
                LaunchedEffect(Unit) {
                    viewModel.loadCurrentUser()
                }

                // 2. On surveille à la fois contactId ET currentUserId.
                // Le code à l'intérieur ne s'exécutera que si les clés changent.
                LaunchedEffect(key1 = contactId, key2 = currentUserId) {
                    // On vérifie que l'ID est bien chargé avant de l'utiliser
                    if (currentUserId != null) {
                        val participants = listOf(currentUserId!!, contactId)
                        Log.d(TAG, "Lancement de la création du chat avec: $participants")

                        viewModel.createOrGetChat(
                            userIds = participants,
                            isPrivate = true
                        )
                    }
                }

                // 3. Gestion de l'affichage : Chargement vs Écran de conversation
                if (currentUserId == null) {
                    // TANT QUE l'ID n'est pas chargé, on affiche une roue de chargement
                    // Cela évite le crash du "currentUserId!!" dans le ConversationScreen
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // UNE FOIS CHARGÉ, on affiche l'écran normal
                    ConversationScreen(
                        contactName = pseudo ?: "User test",
                        messages = messages,
                        onNavigateBack = { finish() },
                        // Ici le !! est sûr car on est dans le bloc "else" du if(currentUserId == null)
                        currentUserId = currentUserId!!,
                        onSentMessage = { messageContent ->
                            viewModel.sendMessage(messageContent, currentUserId!!)
                        }
                    )
                }

                ConversationScreen(
                    contactName = pseudo?: "User test",
                    messages = messages,
                    onNavigateBack = {finish()},
                    currentUserId = viewModel.myUserId ?: userId,
                    onSentMessage = { messageContent ->
                        viewModel.sendMessage(messageContent, userId, contactId )
                    }
                )
            }
        }
    }
}