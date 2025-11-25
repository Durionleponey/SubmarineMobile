package com.example.submarine.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.R
import com.example.submarine.auth.LoginActivity
import com.example.submarine.auth.SignUpActivity
import com.example.submarine.bio.EditBioActivity // <--- IMPORT AJOUTÉ
import com.example.submarine.contacts.ContactsActivity
import com.example.submarine.conversation.ConversationActivity
import com.example.submarine.ui.theme.SubmarineTheme

@Composable
fun HomeScreen() {
    val context = LocalContext.current

    // Utilisation d'un dégradé pour le fond
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- Section Titre et Logo ---
            AppHeader()

            Spacer(modifier = Modifier.height(64.dp))

            // --- Section Boutons d'action ---
            Text(
                text = "Bienvenue à bord",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton "Se Connecter"
            Button(
                onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Se Connecter", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bouton "S'inscrire"
            OutlinedButton(
                onClick = {
                    val intent = Intent(context, SignUpActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    "Créer un compte",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // --- Note de bas de page (Navigation rapide) ---

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, // Espacement équitable
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Contacts
            TextButton(onClick = {
                val intent = Intent(context, ContactsActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Contacts", fontSize = 12.sp)
            }

            // Bouton Bio (AJOUTÉ ICI) 👇
            TextButton(onClick = {
                val intent = Intent(context, EditBioActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Ma Bio", fontSize = 12.sp)
            }

            // Bouton Conversations
            TextButton(onClick = {
                val intent = Intent(context, ConversationActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Chats", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AppHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.submarine),
            contentDescription = "Logo",
            modifier = Modifier.size(100.dp)
        )
        Text(
            text = "Submarine",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun LoginScreenPreview() {
    SubmarineTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            HomeScreen()
        }
    }
}