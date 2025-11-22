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
import com.apollographql.apollo3.mpp.currentThreadId
import com.example.submarine.conversation.UserService


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

        val currentId = intent.getStringExtra("userId") // recup de l'id qui l'a initié mais ici test
        val userId = "6910ae154e5e95f212c42612"



        if (userId == null){
            Log.e(TAG, "L'ID de l'utilisateur n'a pas été transmis.")
            finish()
            return
        }else{
            Log.d(TAG, "L'ID de l'utilisateur est : $userId ")
            viewModel.chargePseudo(userId)

        }

        setContent {
            SubmarineTheme {
                val pseudo by viewModel.userPseudo.collectAsState()

                val message by viewModel.messages.collectAsState()
                val creationState by viewModel.creationState.collectAsState()
                val subState by viewModel.subscriptionState.collectAsState()

                LaunchedEffect(currentId, userId) {
                    val participants = listOf(userId, currentId!!)
                    Log.d(TAG, "Lancement de la création du chat avec: $participants")

                    viewModel.createChat(
                        userIds = participants,
                        isPrivate = true
                    )
                }
                ConversationScreen(
                    contactName = pseudo?: "User test",
                    messages = message,
                    creationState = creationState,
                    subscriptionState = subState,
                    onNavigateBack = {
                        finish()
                    },
                    userId = userId,
                )
            }
        }
    }
}
