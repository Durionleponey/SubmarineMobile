package com.example.submarine.listeContact

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.graphql.GetFriendsListQuery
import com.example.submarine.network.TokenProvider
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.launch

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                ContactsScreen(
                    onBack = { finish() },
                    onAddFriendClick = {
                        val intent = Intent(this, AddContactActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: () -> Unit,
    onAddFriendClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }

    // Liste d'amis venant du backend
    var contacts by remember { mutableStateOf<List<GetFriendsListQuery.FriendsList>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedContact by remember { mutableStateOf<GetFriendsListQuery.FriendsList?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }

    val token = TokenProvider.token

    // Chargement des amis au lancement
    LaunchedEffect(Unit) {
        if (!token.isNullOrEmpty()) {
            try {
                contacts = FriendsApi.getFriendsList(token!!)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erreur lors du chargement"
            }
        }
    }

    // Filtre recherche
    val filtered = contacts.filter { c ->
        (c.pseudo + " " + (c.email ?: "")).contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onAddFriendClick) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = "Ajouter un contact")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher un ami") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                // 🔥 PAS DE key = { ... } → plus de crash d'ID dupliqué
                items(filtered) { contact ->

                    ContactRow(
                        name = contact.pseudo,
                        onClick = {
                            // plus tard : ouvrir un chat par ex.
                        },
                        onLongPress = {
                            selectedContact = contact
                            showActionsDialog = true
                        }
                    )
                }
            }
        }
    }

    // ----- MENU LONG PRESS -----
    if (showActionsDialog && selectedContact != null) {
        val contact = selectedContact!!

        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Options pour ${contact.pseudo}") },
            text = {
                Column {

                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            showBioDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Voir la bio") }

                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Option pas encore disponible")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Créer un groupe") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // ----- POPUP BIO -----
    if (showBioDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Bio de ${contact.pseudo}") },
            text = {
                Text(contact.bio ?: "Aucune bio", fontSize = 16.sp)
            },
            confirmButton = {
                TextButton(onClick = { showBioDialog = false }) { Text("Fermer") }
            }
        )
    }
}

@Composable
private fun ContactRow(
    name: String,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.03f else 1f,
        label = "press-scale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = {
                    isPressed = false
                    onClick()
                },
                onLongClick = {
                    isPressed = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                    isPressed = false
                }
            )
            .padding(16.dp)
    ) {
        Text(text = name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider()
}
