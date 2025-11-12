package com.example.submarine.listeContact

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.submarine.network.TokenProvider
import java.io.IOException
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        if (searchQuery.trim().isEmpty()) {
                            errorMessage = "Veuillez entrer un pseudo à rechercher."
                            return@launch
                        }

                        try {
                            isLoading = true
                            errorMessage = null
                            searchResults = emptyList()

                            val token = TokenProvider.token
                            if (token.isNullOrEmpty()) {
                                errorMessage = "Token manquant — utilisateur non connecté."
                                return@launch
                            }

                            // ✅ Appel à ton API NestJS avec le token en mémoire
                            val results = UserApi.searchUsers(token, searchQuery)
                            if (results.isEmpty()) {
                                errorMessage = "Aucun utilisateur trouvé."
                            } else {
                                searchResults = results
                            }
                        } catch (e: HttpException) {
                            errorMessage = when (e.code()) {
                                401 -> "Non autorisé — token invalide ou expiré."
                                403 -> "Accès refusé."
                                else -> "Erreur du serveur (${e.code()})."
                            }
                        } catch (e: IOException) {
                            errorMessage = "Erreur réseau — impossible de joindre le serveur."
                        } catch (e: Exception) {
                            errorMessage = "Erreur : ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Recherche..." else "Rechercher")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            searchResults.forEach { pseudo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = pseudo,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
