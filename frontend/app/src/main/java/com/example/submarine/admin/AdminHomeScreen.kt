package com.example.submarine.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onNavigateToUsers: () -> Unit,
    // On ajoutera les autres navigations ici plus tard
    // onNavigateToDeletedAccounts: () -> Unit,
    // onNavigateToStats: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administration") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNavigateToUsers,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gestion des Utilisateurs")
            }

            // Bouton pour les comptes supprimés (désactivé pour l'instant)
            Button(
                onClick = { /* TODO */ },
                enabled = false, // On le désactive en attendant de créer l'écran
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Comptes Supprimés (Bientôt)")
            }

            // Bouton pour les statistiques (désactivé pour l'instant)
            Button(
                onClick = { /* TODO */ },
                enabled = false, // On le désactive aussi
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Statistiques (Bientôt)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    SubmarineTheme {
        AdminHomeScreen(
            onNavigateToUsers = {} // Pour la preview, l'action du clic ne fait rien
        )
    }
}