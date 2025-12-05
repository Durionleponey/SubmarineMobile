package com.example.submarine.listeContact

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.submarine.graphql.SendFriendRequestMutation
import com.example.submarine.listeContact.API.GraphQLClient
import com.example.submarine.listeContact.API.UserApi
import com.example.submarine.listeContact.API.UserDto
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<UserDto>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un contact") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(context, PendingRequestsActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Demandes d'amis")
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
                            errorMessage = "Erreur serveur : ${e.code()}"
                        } catch (e: IOException) {
                            errorMessage = "Erreur réseau — serveur inaccessible."
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
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            if (successMessage != null) {
                Text(successMessage!!, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            searchResults.forEach { user ->
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
                        Text(user.pseudo, style = MaterialTheme.typography.bodyLarge)

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

                                        // ✅ On envoie l’ID, pas le pseudo
                                        val mutation = SendFriendRequestMutation(user._id)

                                        val response = withContext(Dispatchers.IO) {
                                            client.mutation(mutation).execute()
                                        }

                                        val status = response.data?.sendFriendRequest?.status
                                        if (status != null) {
                                            successMessage = "Demande envoyée ($status)"
                                        } else {
                                            errorMessage =
                                                response.errors?.firstOrNull()?.message
                                                    ?: "Erreur inconnue."
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
