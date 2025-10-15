package com.example.submarine.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview   // <-- CET IMPORT
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.model.Contact
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.compose.material.icons.filled.PersonAdd


class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                ContactsScreen(onBack = { finish() }) // ferme l’activité
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val temps = 2 // donnée arbitraire mtn mais change après
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
                        // ajoute la section "actions" pour mettre d'autres icones
                actions = {
                    IconButton(onClick = { /* TODO : Naviguer vers l'écran d'ajout d'amis */ }) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Ajouter un contact"
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
            onBack = {}, // fonction de retour à définir
        )
    }
}