package com.example.submarine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.auth.LoginActivity
import com.example.submarine.auth.Signup
import com.example.submarine.contacts.ContactsActivity
import com.example.submarine.conversation.ConversationActivity
import com.example.submarine.ui.theme.SubmarineTheme

class WelcomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubmarineTheme {
                WelcomeScreen()
            }
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Section Logo et Titre ---
            Icon(
                imageVector = Icons.Filled.Water,
                contentDescription = "Submarine Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Bienvenue sur Submarine",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Plongez dans la conversation",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // --- Section Boutons Principaux ---
            Button(
                onClick = {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Se connecter")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, Signup::class.java))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("S'inscrire")
            }

            Spacer(modifier = Modifier.height(48.dp))

            // --- Section Boutons de Débogage Temporaires ---
            Text(
                text = "Accès rapide (temporaire)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { context.startActivity(Intent(context, ConversationActivity::class.java)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Conversations")
                }
                Button(
                    onClick = { context.startActivity(Intent(context, ContactsActivity::class.java)) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Contacts")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    SubmarineTheme {
        WelcomeScreen()
    }
}
