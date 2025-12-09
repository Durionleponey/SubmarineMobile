package com.example.submarine.conversation.tests

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.submarine.conversation.ConversationViewModel
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.compose.runtime.collectAsState
class ConversationTestActivity : ComponentActivity() {

    private val TAG = "ConversationTestActivity"
    private val viewModel: ConversationViewModel by viewModels()

    // VOS IDS DE TEST (Ceux que vous avez fournis)
    // USER A = Vous ("6913...")
    // USER B = Le contact ("6910...")
    private val USER_A_ID = "6913411dce7e0315c88b7533"
    private val USER_B_ID = "6930091a2fc453e8b84d1b52"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optionnel : Désactiver la sécurité d'écran pour faciliter les screenshots de debug
        // window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setContent {
            SubmarineTheme {
                // État pour savoir si on est sur l'écran de choix ou sur le chat
                var isChatMode by remember { mutableStateOf(false) }

                // État pour stocker qui est "Je" et qui est "L'autre" pour cette session
                var currentUserId by remember { mutableStateOf("") }
                var currentContactId by remember { mutableStateOf("") }
                var currentContactName by remember { mutableStateOf("") }

// ConversationTestActivity.kt (dans onCreate)
                val isEncryptionEnabled by viewModel.isEncryptionEnabled.collectAsState()
                val toggleEncryption = { enabled: Boolean -> viewModel.toggleEncryption(enabled) }
                if (!isChatMode) {
                    // --- ÉCRAN 1 : SÉLECTION DU RÔLE ---
                    UserSelectionScreen(
                        onSelectUserA = {
                            currentUserId = USER_A_ID
                            currentContactId = USER_B_ID
                            currentContactName = "Contact (B)"
                            isChatMode = true
                        },
                        onSelectUserB = {
                            currentUserId = USER_B_ID
                            currentContactId = USER_A_ID
                            currentContactName = "Moi (A)"
                            isChatMode = true
                        },
                        isEncryptionEnabled = isEncryptionEnabled,
                        onToggleEncryption = toggleEncryption

                    )
                } else {
                    // --- ÉCRAN 2 : LE CHAT ---
                    TestChatScreen(
                        viewModel = viewModel,
                        myUserId = currentUserId,
                        contactId = currentContactId,
                        contactName = currentContactName,
                        onBack = {
                            // Retour au menu de sélection
                            isChatMode = false
                            // Important : on pourrait vouloir reset le VM ici si nécessaire
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserSelectionScreen(
    onSelectUserA: () -> Unit,
    onSelectUserB: () -> Unit,
    isEncryptionEnabled: Boolean,          // <--- AJOUTER ÇA
    onToggleEncryption: (Boolean) -> Unit  // <--- ET AJOUTER ÇA

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QUI ÊTES-VOUS ?", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSelectUserA,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Utilisateur A (Moi)\nID: ...7533")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSelectUserB,
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Utilisateur B (Contact)\nID: ...2612")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Cliquez sur l'un pour simuler l'envoi.\nRevenez en arrière et cliquez sur l'autre pour voir la réception.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = if (isEncryptionEnabled) "CHIFFREMENT ON" else "CHIFFREMENT OFF",
                color = if (isEncryptionEnabled) Color.Green else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(16.dp))
            Switch(
                checked = isEncryptionEnabled,
                onCheckedChange = onToggleEncryption // Maintenant, cette variable existe !
            )
        }
    }
}

// ConversationTestActivity.kt

@Composable
fun TestChatScreen(
    viewModel: ConversationViewModel,
    myUserId: String,
    contactId: String,
    contactName: String,
    onBack: () -> Unit
) {
    // Initialisation du Chat
    LaunchedEffect(key1 = contactId) {
        viewModel.myUserId = myUserId

        // IMPORTANT : On trie les IDs pour garantir que c'est la MEME conversation
        // peu importe si on est A ou B.
        val participants = listOf(myUserId, contactId).sorted()

        Log.d("TestChat", "Lancement init chat. Participants triés: $participants")

        viewModel.createOrGetChat(
            userIds = participants,
            isPrivate = true
        )

        // On charge aussi le pseudo pour le header
        viewModel.chargePseudo(contactId)
    }

    val messages by viewModel.messages.collectAsState()
    val pseudoReel by viewModel.userPseudo.collectAsState()

    val isEncryptionEnabled by viewModel.isEncryptionEnabled.collectAsState()

    // Affichage d'un bandeau de debug en haut
    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFE082)) // Jaune pour signaler mode TEST
                .padding(4.dp)
        ) {
            Text(
                text = "MODE TEST | Connecté: ${myUserId.takeLast(4)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black
            )
        }


        ConversationScreen(
            contactName = if(pseudoReel != "Chargement...") pseudoReel else contactName,
            messages = messages,
            onNavigateBack = onBack,
            currentUserId = myUserId,
            onSentMessage = { messageContent ->
                viewModel.sendMessage(messageContent, myUserId, contactId)
            },
            isEncryptionEnabled = isEncryptionEnabled,
            onToggleEncryption = { enabled ->
                viewModel.toggleEncryption(enabled)
            }

        )
    }
}