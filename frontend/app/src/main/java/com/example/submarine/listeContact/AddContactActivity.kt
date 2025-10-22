package com.example.submarine.listeContact

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.apollographql.apollo3.exception.ApolloException
import com.example.submarine.graphql.SearchUserQuery // <-- IMPORTATION CLÉ
import com.example.submarine.network.apolloClient // <-- IMPORTATION CLÉ
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
class AddContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                AddContactScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(onBack: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    // 1. On change le type pour utiliser la classe générée par Apollo
    var foundUsers by remember { mutableStateOf<List<SearchUserQuery.User>?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var searchJob by remember { mutableStateOf<Job?>(null) }

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
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newQuery ->
                    searchQuery = newQuery
                    foundUsers = null // On réinitialise les utilisateurs trouvés
                    searchJob?.cancel()

                    if (newQuery.isNotBlank()) {
                        isLoading = true
                        searchJob = coroutineScope.launch {
                            delay(500)
                            try {
                                // 2. On remplace l'appel Retrofit par l'appel Apollo
                                val response = apolloClient.query(SearchUserQuery(search = newQuery)).execute()
                                Log.d("APOLLO_CALL", "Recherche de: $newQuery. Réponse: ${response.data}")

                                if (response.data != null) {
                                    // La liste des utilisateurs est dans response.data.users
                                    foundUsers = response.data?.users
                                } else if (response.hasErrors()) {
                                    Log.e("APOLLO_CALL", "Erreurs GraphQL: ${response.errors}")
                                }
                            } catch (e: ApolloException) {
                                Log.e("APOLLO_CALL", "Erreur de connexion Apollo !", e)
                                // Vous pouvez afficher un Toast ici si vous le souhaitez
                            } finally {
                                isLoading = false
                            }
                        }
                    } else {
                        isLoading = false
                    }
                },
                label = { Text("Rechercher un utilisateur...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (!foundUsers.isNullOrEmpty()) {
                // 3. On affiche la liste des utilisateurs trouvés
                // Pour l'instant, on n'en affiche qu'un pour garder le code simple
                FoundUserItem(user = foundUsers!!.first())
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Pour l'instant, on se concentre sur le premier utilisateur trouvé
                    foundUsers?.firstOrNull()?.let { user ->
                        // TODO: Remplacer l'envoi de la demande d'ami par une mutation GraphQL
                        // Pour l'instant, on affiche juste un Toast
                        Toast.makeText(context, "TODO: Envoyer une demande d'ami à ${user.pseudo} !", Toast.LENGTH_SHORT).show()
                        // onBack()
                    }
                },
                // Le bouton est désactivé tant qu'aucun utilisateur n'est trouvé
                enabled = !foundUsers.isNullOrEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Envoyer une demande d’ami")
            }
        }
    }
}

// 4. On adapte le Composable pour utiliser le nouveau type d'utilisateur
@Composable
fun FoundUserItem(user: SearchUserQuery.User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, shape = MaterialTheme.shapes.medium)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Utilisateur trouvé",
            tint = Color(0xFF00C853),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9))
                .padding(8.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            // Le champ s'appelle 'pseudo', comme défini dans votre fichier .graphql
            text = user.pseudo,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
