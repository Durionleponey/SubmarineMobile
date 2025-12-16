package com.example.submarine.auth

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions // AJOUT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType // AJOUT
import androidx.compose.ui.text.input.PasswordVisualTransformation // AJOUT
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
            modifier = Modifier.fillMaxWidth(),
            // Optimisation pour le clavier email
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        // --- CHAMP MOT DE PASSE MODIFIÉ ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            // Masque le texte (points noirs)
            visualTransformation = PasswordVisualTransformation(),
            // Indique au clavier que c'est un mot de passe
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
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

                        try {
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
                                    Log.d("auth"," Account created! Welcome $userPseudo ($userEmail)")

                                    val intent = Intent(context, LoginActivity::class.java).apply {
                                        putExtra("email", email)
                                        putExtra("password", password)
                                    }
                                    context.startActivity(intent)
                                } else {
                                    Log.e("auth","❌ Erreur de création : ${response.code()}")

                                    // --- CORRECTION ICI ---
                                    // On récupère la liste des erreurs
                                    val errors = response.body()?.errors
                                    // On prend la première, on la caste en Map, et on lit "message"
                                    val firstError = errors?.firstOrNull() as? Map<*, *>
                                    val errorMsg = firstError?.get("message")?.toString()
                                        ?: "Invalid email or password is not secure enough"

                                    message = "❌ Error: $errorMsg"
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("auth", "Erreur réseau", e)
                            withContext(Dispatchers.Main) {
                                message = "❌ Network error"
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