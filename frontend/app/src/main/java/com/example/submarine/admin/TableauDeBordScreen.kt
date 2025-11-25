package com.example.submarine.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.submarine.admin.composants.UtilisateurListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableauDeBordScreen(
    viewModel: TableauDeBordViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<AdminUser?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tableau de Bord") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (showDialog && userToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    userToDelete = null
                },
                title = { Text("Confirmation") },
                text = { Text("Êtes-vous sûr de vouloir supprimer l'utilisateur \"${userToDelete!!.name}\" ?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.supprimerUtilisateur(userToDelete!!.id)
                            showDialog = false
                            userToDelete = null
                        }
                    ) {
                        Text("Oui, supprimer")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                            userToDelete = null
                        }
                    ) {
                        Text("Annuler")
                    }
                }
            )
        }
        if (state.users.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Tous les utilisateurs ont été supprimés.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(
                    items = state.users,
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

@Preview(showBackground = true)
@Composable
fun TableauDeBordScreenPreview() {
    TableauDeBordScreen()
}