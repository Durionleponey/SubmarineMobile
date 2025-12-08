package com.example.submarine.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.example.submarine.conversation.ConversationActivity
import com.example.submarine.conversation.tests.ConversationTestActivity
import com.example.submarine.elec.LcdActivity
import com.example.submarine.listeContact.ContactsActivity
import com.example.submarine.ui.theme.SubmarineTheme

@Composable
fun HomeScreen() {
    val context = LocalContext.current

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
            // --- Titre + Logo ---
            AppHeader()

            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Bienvenue à bord",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Bouton "Se connecter"
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

            // Bouton "Créer un compte"
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

        // --- Barre du bas ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Liste contacts
            TextButton(
                onClick = {
                    val intent = Intent(context, ContactsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Liste Contacts",
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Conversations
            TextButton(
                onClick = {
                    val intent = Intent(context, ConversationActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Conversations",
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // TEST Conversations
            TextButton(
                onClick = {
                    val intent = Intent(context, ConversationTestActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "TEST Conversations",
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Électronique
            TextButton(
                onClick = {
                    val intent = Intent(context, LcdActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Contact-help",
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                    // plus de maxLines / softWrap ici
                )
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
fun HomeScreenPreview() {
    SubmarineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen()
        }
    }
}
