package com.example.submarine.contacts

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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.model.Contact
import com.example.submarine.ui.theme.SubmarineTheme
import com.example.submarine.listeContact.AddContactActivity

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
    // 🔹 Liste temporaire (sera remplacée plus tard par les vrais contacts backend)
    var searchQuery by remember { mutableStateOf("") }
    val allContacts = listOf(
        Contact("Martin Dupont", "Message non lu", "Il y a 2 heures"),
        Contact("Bob Jones", "Message non lu", "Il y a 3 jours"),
        Contact("Mick Gordon", "Message lu", "5/10/2025")
    )

    // 🔹 Filtrage dynamique (basé sur ton ancien code)
    val filteredContacts = allContacts.filter {
        it.nom.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes Contacts") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = onAddFriendClick) {
                        Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = "Ajouter un contact")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // 🔹 Barre de recherche intégrée (basée sur ton idée)
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
                items(filteredContacts) { contact ->
                    ContactItem(contact)
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: Contact) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: ouvrir la conversation */ }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = contact.nom, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(text = contact.derniereConnexion, fontSize = 16.sp)
        }
        Text(text = contact.dernierMessage, fontSize = 16.sp)
    }
    HorizontalDivider()
}
