package com.example.submarine.listeContact

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.submarine.network.TokenProvider
import com.example.submarine.graphql.SendFriendRequestMutation
import com.example.submarine.listeContact.GraphQLClient
import com.apollographql.apollo3.api.ApolloResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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

            // 🔍 Bouton de recherche
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
                            successMessage = null
                            searchResults = emptyList()

                            val token = TokenProvider.token
                            if (token.isNullOrEmpty()) {
                                errorMessage = "Token manquant — utilisateur non connecté."
                                return@launch
                            }

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

            // 🔴 Erreurs
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 🟢 Succès
            if (successMessage != null) {
                Text(
                    text = successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 🧑 Liste des utilisateurs trouvés
            searchResults.forEach { pseudo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(pseudo, style = MaterialTheme.typography.bodyLarge)

                        Button(
                            onClick = {
                                scope.launch {
                                    val token = TokenProvider.token
                                    if (token.isNullOrEmpty()) {
                                        errorMessage = "Utilisateur non connecté."
                                        return@launch
                                    }

                                    try {
                                        val client = GraphQLClient.client.newBuilder()
                                            .addHttpHeader("Authorization", "Bearer $token")
                                            .build()

                                        val mutation = SendFriendRequestMutation(pseudo) // ⚠️ remplacer par ID réel
                                        val response: ApolloResponse<SendFriendRequestMutation.Data> =
                                            withContext(Dispatchers.IO) {
                                                client.mutation(mutation).execute()
                                            }

                                        val status = response.data?.sendFriendRequest?.status
                                        if (status != null) {
                                            successMessage = "Demande envoyée ($status)"
                                        } else {
                                            errorMessage =
                                                response.errors?.firstOrNull()?.message ?: "Erreur inconnue."
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erreur : ${e.message}"
                                    }
                                }
                            }
                        ) {
                            Text("Ajouter")
                        }
                    }
                }
            }
        }
    }
}
