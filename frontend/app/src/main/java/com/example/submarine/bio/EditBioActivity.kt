package com.example.submarine.bio

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import android.util.Log

class EditBioActivity : ComponentActivity() {

    private val viewModel: EditBioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("GraphQL", "🚀 loadUserBio() appelée")

        // 🔄 Charge la bio dès que l’écran s’ouvre
        viewModel.loadUserBio()

        setContent {
            SubmarineTheme {
                // 🔄 Observe les flux du ViewModel
                val username by viewModel.username.collectAsState()
                val bio by viewModel.bio.collectAsState()
                val status by viewModel.updateStatus.collectAsState()

                // 🎨 Écran principal
                EditBioScreen(
                    username = username,
                    bio = bio,
                    onBioChange = { viewModel.onBioChange(it) },
                    onSave = { viewModel.saveBio() }
                )

                // ✅ Affiche le toast de confirmation
                status?.let {
                    LaunchedEffect(it) {
                        Toast.makeText(this@EditBioActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}

@Composable
fun EditBioScreen(
    username: String,
    bio: String,
    onBioChange: (String) -> Unit,
    onSave: () -> Unit
) {
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

        // Champ bio + compteur de caractères
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = bio,
                onValueChange = {
                    if (it.length <= 150) onBioChange(it) // limite de 150 caractères
                },
                label = { Text("Ta bio") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )

            // 🧮 Compteur en haut à droite
            Text(
                text = "${bio.length}/150",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                color = if (bio.length > 140)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Bouton de sauvegarde
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sauvegarder")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditBioScreenPreview() {
    SubmarineTheme {
        EditBioScreen(
            username = "Jean Dupont",
            bio = "Voici ma bio actuelle",
            onBioChange = {},
            onSave = {}
        )
    }
}
