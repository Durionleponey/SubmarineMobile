package com.example.submarine.conversation

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.compose.runtime.collectAsState
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue


class ConversationActivity : ComponentActivity() {

    private val TAG = "ConversationActivity"
    private val viewModel: ConversationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Log.d(TAG, "FLAG_SECURE activé.")

        val contactId = "6910ae154e5e95f212c42612"
        val userId = "6913411dce7e0315c88b7533"


        if (contactId == null){
            Log.e(TAG, "L'ID de l'utilisateur n'a pas été transmis.")
            finish()
            return
        }else{
            Log.d(TAG, "L'ID de l'utilisateur est : $contactId ")
            viewModel.chargePseudo(contactId)

        }

        setContent {
            SubmarineTheme {
                val pseudo by viewModel.userPseudo.collectAsState()

                val messages by viewModel.messages.collectAsState()
                //val creationState by viewModel.creationState.collectAsState()
               // val subState by viewModel.subscriptionState.collectAsState()

                LaunchedEffect(key1 = contactId) {

                    viewModel.myUserId = userId

                    val participants = listOf(userId,contactId)
                    Log.d(TAG, "Lancement de la création du chat avec: $participants")

                    viewModel.createOrGetChat(
                        userIds = participants,
                        isPrivate = true
                    )
                }
                ConversationScreen(
                    contactName = pseudo?: "User test",
                    messages = messages,
                    onNavigateBack = {finish()},
                    currentUserId = viewModel.myUserId ?: userId,
                    onSentMessage = { messageContent ->
                        viewModel.sendMessage(messageContent, userId)
                    }
                )
            }
        }
    }
}
