package com.example.submarine.contacts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.submarine.listeContact.AddContactActivity
import com.example.submarine.model.Contact
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.launch
import androidx.compose.animation.core.animateFloatAsState


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

    val contacts = remember {
        mutableStateListOf(
            Contact("Martin", "Dupont", "martin_dpt", "stage dans la poche!"),
            Contact("Alice", "Lambert", "alice_waves", "J'adore le monde sous-marin."),
            Contact("Bob", "Jones", "bobby_j", "Joyeux anniversaire bébé."),
            Contact("Lebron", "King James", "KingJames", "Je suis la définition de l'expression 'le travail paye''"),
            Contact("Kevin", "Durand", "k_durand", "Sad ajd.."),
        )
    }

    val customNicknames = remember { mutableStateMapOf<Contact, String>() }

    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var showActionsDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var newPseudo by remember { mutableStateOf("") }

    // ✅ Pour la bio
    var showBioDialog by remember { mutableStateOf(false) }

    val filtered = contacts.filter { c ->
        val displayName = customNicknames[c] ?: "${c.prenom} ${c.nom}"
        (displayName + " " + c.pseudo).contains(searchQuery, ignoreCase = true)
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
                label = { Text("Rechercher un contact") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(filtered, key = { "${it.prenom}_${it.nom}_${it.pseudo}" }) { contact ->
                    val displayName = customNicknames[contact] ?: "${contact.prenom} ${contact.nom}"

                    ContactRow(
                        name = displayName,
                        onClick = {},
                        onLongPress = {
                            selectedContact = contact
                            newPseudo = customNicknames[contact] ?: displayName
                            showActionsDialog = true
                        }
                    )
                }
            }
        }
    }

    /* ----- MENU LONG PRESS ----- */
    if (showActionsDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Options pour ${customNicknames[contact] ?: "${contact.prenom} ${contact.nom}"}") },
            text = {
                Column {

                    // ✅ Voir la bio
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            showBioDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Voir la bio") }

                    // Modifier pseudo
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            showEditDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Définir le pseudo") }

                    // Créer un groupe (fake)
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Fonction groupe pas encore disponible")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Créer un groupe") }

                    // Sourdine (fake)
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Fonctionnalité de sourdine pas encore disponible")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Mettre en sourdine") }

                    // Supprimer
                    TextButton(
                        onClick = {
                            showActionsDialog = false
                            contacts.remove(contact)
                            customNicknames.remove(contact)
                            scope.launch {
                                snackbarHostState.showSnackbar("Contact supprimé de la liste")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Supprimer le contact") }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    /* ----- POPUP BIO ----- */
    if (showBioDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showBioDialog = false },
            title = { Text("Bio de ${contact.prenom}") },
            text = {
                Text(contact.bio, fontSize = 16.sp)
            },
            confirmButton = {
                TextButton(onClick = { showBioDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    /* ----- DIALOG EDIT PSEUDO ----- */
    if (showEditDialog && selectedContact != null) {
        val contact = selectedContact!!
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier le pseudo") },
            text = {
                OutlinedTextField(
                    value = newPseudo,
                    onValueChange = { newPseudo = it },
                    singleLine = true,
                    label = { Text("Surnom affiché") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        customNicknames[contact] =
                            newPseudo.trim().ifEmpty { "${contact.prenom} ${contact.nom}" }
                        showEditDialog = false
                    }
                ) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Annuler") }
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

    // ✅ Animation scale
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
