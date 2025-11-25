package com.example.submarine.contacts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp // <-- Import pour l'icône de sortie
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.MainActivity // Ton écran de Login
import com.example.submarine.listeContact.AddContactActivity
import com.example.submarine.model.Contact
import com.example.submarine.network.RetrofitInstance // Assure-toi que cet import existe
import com.example.submarine.network.TokenProvider   // Assure-toi que cet import existe
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                ContactsScreen(
                    onBack = { finish() }, // ferme l'activité
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
fun ContactsScreen(onBack: () -> Unit, onAddFriendClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Pour lancer la déconnexion asynchrone

    val temps = 2
    val contacts = listOf(
        Contact("Alice Dupont", "Message non lu", "Il y a $temps heures"),
        Contact("Bob Martin", "Message non lu", "Il y a $temps Jours"),
        Contact("Charlie Durand", "Message lu", "5/10/2025")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                // Section Actions (Côté droit)
                actions = {
                    // 1. Bouton Ajouter Ami
                    IconButton(onClick = onAddFriendClick) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Ajouter un contact"
                        )
                    }

                    // 2. Bouton Déconnexion (Le petit truc ajouté)
                    IconButton(onClick = {
                        scope.launch(Dispatchers.IO) {
                            // A. Prévenir le serveur
                            try {
                                RetrofitInstance.authApi.logout()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                            // B. Nettoyer le téléphone et rediriger
                            withContext(Dispatchers.Main) {
                                // Reset token mémoire
                                TokenProvider.token = null

                                // Reset préférences
                                val sharedPref = context.getSharedPreferences("submarine_prefs", Context.MODE_PRIVATE)
                                with(sharedPref.edit()) {
                                    clear()
                                    apply()
                                }

                                // Redirection vers Login
                                val intent = Intent(context, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Se déconnecter",
                            tint = MaterialTheme.colorScheme.error // Rouge pour indiquer la sortie/danger
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(contacts) { contact ->
                ContactItem(contact)
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // plus tard : ouvrir ChatActivity(contact.nom)
            }
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
        {
            Text(text = contact.nom, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = contact.derniereConnexion, fontSize = 16.sp)
        }

        Text(text = contact.dernierMessage, fontSize = 16.sp)
    }
    HorizontalDivider()
}

@Preview(showBackground = true)
@Composable
fun PreviewContactsScreen() {
    SubmarineTheme {
        ContactsScreen(
            onBack = {},
            onAddFriendClick = {}
        )
    }
}