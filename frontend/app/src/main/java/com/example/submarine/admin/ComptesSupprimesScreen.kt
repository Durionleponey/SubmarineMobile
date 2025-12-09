package com.example.submarine.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.submarine.ui.theme.SubmarineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComptesSupprimesScreen(
    utilisateursSupprimes: List<AdminUser>,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comptes Supprimés") },
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
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(utilisateursSupprimes) { user ->
                ListItem(
                    headlineContent = { Text(user.name) },
                )
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true, name = "Écran des Comptes Supprimés")
@Composable
fun ComptesSupprimesScreenPreview() {
    // 1. On crée une fausse liste d'utilisateurs supprimés pour la preview
    val sampleDeletedUsers = listOf(
        AdminUser(id = 101, name = "AncienUtilisateur1"),
        AdminUser(id = 102, name = "AncienUtilisateur2"),
        AdminUser(id = 103, name = "AncienUtilisateur3")
    )

    // 2. On enveloppe notre écran dans le thème de l'application
    SubmarineTheme {
        ComptesSupprimesScreen(
            // 3. On passe les fausses données à l'écran
            utilisateursSupprimes = sampleDeletedUsers,
            // 4. On passe une action vide pour la navigation
            onNavigateBack = {}
        )
    }
}