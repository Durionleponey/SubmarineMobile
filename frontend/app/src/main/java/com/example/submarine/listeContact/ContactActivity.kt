package com.example.submarine.contacts

import androidx.compose.material3.CenterAlignedTopAppBar
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    val contacts = listOf(
        Contact("Alice Dupont", "0470 12 34 56"),
        Contact("Bob Martin", "0499 98 76 54"),
        Contact("Charlie Durand", "0485 55 66 77")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mes Contacts") })
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
        Text(text = contact.nom, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = contact.numero, fontSize = 16.sp)
    }
    Divider()
}
