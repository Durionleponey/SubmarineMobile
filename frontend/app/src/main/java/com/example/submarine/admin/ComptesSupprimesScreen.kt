package com.example.submarine.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.submarine.admin.composants.UtilisateurSupprimeListItem
import com.example.submarine.graphql.type.UserStatus



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComptesSupprimesScreen(
    onNavigateBack: () -> Unit,
    viewModel: TableauDeBordViewModel // On reçoit le ViewModel partagé
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var userToReactivate by remember { mutableStateOf<AdminUser?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = remember(searchQuery, uiState.users) {
        val deletedUsers = uiState.users.filter { it.status == UserStatus.DELETED }

        if (searchQuery.isBlank()) {
            deletedUsers
        } else {
            deletedUsers.filter {
                it.pseudo.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
        if (showDialog && userToReactivate != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = { Text("Confirmation de Réactivation") },
                text = { Text("Êtes-vous sûr de vouloir réactiver le compte \"${userToReactivate!!.pseudo}\" ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            userToReactivate?.id?.let { viewModel.reactiverUtilisateur(it) }
                            showDialog = false
                        }
                    ) {
                        Text("Oui, réactiver")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
        Column(modifier = Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                label = { Text("Rechercher par pseudo...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Icône de recherche"
                    )
                },
                singleLine = true
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator()
                    }

                    uiState.error != null -> {
                        Text(
                            text = "Erreur de chargement :\n${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    filteredUsers.isEmpty() -> {
                        Text(
                            text = if (searchQuery.isBlank()) "Aucun compte supprimé."
                            else "Aucun compte trouvé pour \"$searchQuery\"",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(
                                items = filteredUsers,
                                key = { user -> user.id }
                            ) { user ->
                                UtilisateurSupprimeListItem(
                                    user = user,
                                    onReactivateClick = {
                                        userToReactivate = user
                                        showDialog = true
                                    }

                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

/*@Preview(showBackground = true, name = "Écran des Comptes Supprimés")
@Composable
fun ComptesSupprimesScreenPreview() {
    val sampleUsers = listOf(
        AdminUser(4, "Dave"),
        AdminUser(5, "Eve")
    )
    SubmarineTheme {
        ComptesSupprimesScreenContent(
            utilisateursSupprimes = sampleUsers,
            onReactivateClick = {},
            onNavigateBack = {}
        )
    }
}*/