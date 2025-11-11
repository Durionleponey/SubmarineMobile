package com.example.submarine.auth

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignUpScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val mutation = """
                        mutation {
                          createUser(createUserInput: {
                            email: "$email",
                            password: "$password"
                          }) {
                            _id
                            email
                            pseudo
                            bio
                          }
                        }
                    """.trimIndent()

                    val request = GraphQLRequest(query = mutation)

                    val response = RetrofitInstance.graphqlApi.executeGraphQL<Map<String, Any>>(
                        token = "",
                        request = request
                    )

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body()?.data != null) {
                            val user = response.body()?.data?.get("createUser") as? Map<*, *>
                            val userEmail = user?.get("email") as? String
                            val userPseudo = user?.get("pseudo") as? String
                            message = "✅ Account created! Welcome $userPseudo ($userEmail)"

                            // Redirection vers la page de login avec préremplissage
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                putExtra("email", email)
                                putExtra("password", password)
                            }
                            context.startActivity(intent)
                        } else {
                            message = "❌ Error creating account: invalid email or password is not secure enough"
                            message = "❌ Error: ${response.errorBody()?.string() ?: response.message()}"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        message?.let {
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }
    }
}
