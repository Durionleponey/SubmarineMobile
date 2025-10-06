package com.example.submarine.bio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme

class EditBioActivity : ComponentActivity() {
    override fun onCreate(initialState: Bundle?) {
        super.onCreate(initialState)
        setContent {
            SubmarineTheme {
                EditBioScreen(
                    username = "Jean Dupont",
                    initialBio = "Voici ma bio actuelle"
                )
            }
        }
    }
}

@Composable
fun EditBioScreen(
    username: String,
    initialBio: String,
    onSave: (String) -> Unit = {}
) {
    var bio by remember { mutableStateOf(initialBio) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nom utilisateur
        Text(
            text = username,
            style = MaterialTheme.typography.headlineSmall
        )

        // Champ bio
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Ta bio") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        // Bouton de sauvegarde
        Button(
            onClick = { onSave(bio) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sauvegarder")
        }
    }
}

