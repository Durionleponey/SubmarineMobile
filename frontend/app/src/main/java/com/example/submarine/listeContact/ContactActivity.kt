package com.example.submarine.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.model.Contact
import com.example.submarine.ui.theme.SubmarineTheme

class ContactsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                ContactsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen() {
    // TODO: remplacera par les données du backend
    val contacts = remember {
        listOf(
            Contact("Alice Dupont", "0470 12 34 56"),
            Contact("Bob Martin", "0499 98 76 54"),
            Contact("Charlie Durand", "0485 55 66 77"),
            Contact("David Leroy", "0488 77 55 22"),
            Contact("LeBron Raymond James", "0488 23 32 23")

        )
    }

    // État de la recherche
    var searchQuery by remember { mutableStateOf("") }

    // Filtrage insensible à la casse sur le nom
    val filteredContacts = remember(searchQuery, contacts) {
        contacts.filter { it.nom.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Contacts") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barre de recherche avec icône loupe
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher dans Submarine") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Recherche") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            if (filteredContacts.isEmpty()) {
                Text(
                    text = "Aucun contact trouvé",
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredContacts) { contact ->
                        ContactItem(contact)
                    }
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
            .clickable {
                // TODO: plus tard : ouvrir la conversation ou la fiche du contact
            }
            .padding(16.dp)
    ) {
        Text(text = contact.nom, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = contact.numero, fontSize = 16.sp)
    }
    Divider()
}
