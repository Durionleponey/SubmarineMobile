package com.example.submarine.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToDeletedUsers: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
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

            // Bouton pour les comptes supprimés
            Button(
                onClick = onNavigateToDeletedUsers,
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Comptes Supprimés")
            }

            // Bouton pour les statistiques
            Button(
                onClick = onNavigateToStats,
                enabled = true,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Statistiques")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
        AdminHomeScreen(
            onNavigateBack = {},
            onNavigateToUsers = {},
            onNavigateToStats = {},
            onNavigateToDeletedUsers = {}
        )

}