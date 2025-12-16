package com.example.submarine.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.filter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.submarine.admin.composants.UtilisateurListItem
import com.example.submarine.type.UserStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableauDeBordScreen(
    onNavigateBack: () -> Unit,
    viewModel: TableauDeBordViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<AdminUser?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = remember(searchQuery, uiState.users) {
        val activeUsers = uiState.users.filter { it.status == UserStatus.ACTIVE }
        if (searchQuery.isBlank()) {
            activeUsers
        } else {
            activeUsers.filter {
                it.pseudo.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestion des Utilisateurs Actif") },
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
        if (showDialog && userToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                },
                title = { Text("Confirmation") },
                text = { Text("Êtes-vous sûr de vouloir supprimer l'utilisateur \"${userToDelete!!.pseudo}\" ?") },
                confirmButton = {
                    Button(
                        onClick = {
                            userToDelete?.id?.let { viewModel.supprimerUtilisateur(it) }
                            showDialog = false
                        }
                    ) {
                        Text("Oui, supprimer")
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
        Column(
            modifier = Modifier.padding(innerPadding),
            //horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Le champ de texte pour la recherche
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                label = { Text("Rechercher par pseudo...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Icône de recherche") },
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
                            text = if (searchQuery.isBlank()) "Aucun utilisateur actif."
                            else "Aucun utilisateur trouvé pour \"$searchQuery\"",
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = filteredUsers,
                                key = { user -> user.id }
                            ) { user ->
                                UtilisateurListItem(
                                    user = user,
                                    onDeleteClick = {
                                        userToDelete = user
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

/*@Preview(showBackground = true)
@Composable
fun TableauDeBordScreenPreview() {
    TableauDeBordScreen(onNavigateBack = {})
}*/
