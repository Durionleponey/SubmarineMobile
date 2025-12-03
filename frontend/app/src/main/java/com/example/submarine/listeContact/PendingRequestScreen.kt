package com.example.submarine.listeContact

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.launch
import com.example.submarine.graphql.GetPendingFriendRequestsQuery
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.submarine.listeContact.API.FriendsApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingRequestsScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()

    var requests by remember {
        mutableStateOf<List<GetPendingFriendRequestsQuery.PendingRequest>>(emptyList())
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val token = TokenProvider.token

    LaunchedEffect(Unit) {
        if (!token.isNullOrEmpty()) {
            try {
                requests = FriendsApi.getPendingRequests(token)
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demandes d'amis reçues") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            requests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Demande de : ${req.sender.pseudo}")

                        Spacer(Modifier.height(10.dp))

                        Row {
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        FriendsApi.acceptFriendRequest(token!!, req._id)
                                        successMessage = "Demande acceptée"
                                        requests = requests.filter { it._id != req._id }
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            }) {
                                Text("Accepter")
                            }

                            Spacer(Modifier.width(12.dp))

                            Button(onClick = {
                                scope.launch {
                                    try {
                                        FriendsApi.rejectFriendRequest(token!!, req._id)
                                        successMessage = "Demande refusée"
                                        requests = requests.filter { it._id != req._id }
                                    } catch (e: Exception) {
                                        errorMessage = e.message
                                    }
                                }
                            }) {
                                Text("Refuser")
                            }
                        }
                    }
                }
            }
        }
    }
}
