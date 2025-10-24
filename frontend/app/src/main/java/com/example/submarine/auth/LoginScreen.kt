package com.example.submarine.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.submarine.MainActivity
import com.example.submarine.ui.theme.SubmarineTheme
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// La classe de l'activité reste inchangée
class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                LoginScreen(
                    onLoginSuccess = {
                        // Rediriger vers l'activité principale
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Empêche l'utilisateur de revenir à l'écran de connexion
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager: SessionManager = remember { SessionManager(context) }
    val client = remember { OkHttpClient() }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion") },
                navigationIcon = {
                    IconButton(onClick = {
                        // Ferme l'activité actuelle et revient à la précédente (WelcomeActivity)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Applique le padding de la Scaffold
                .padding(horizontal = 32.dp), // Garde votre padding horizontal
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val token = withContext(Dispatchers.IO) {
                                val url = "http://10.0.2.2:3000/auth/login"
                                val jsonBody = JSONObject().apply {
                                    put("email", email)
                                    put("password", password)
                                }.toString()
                                val requestBody = jsonBody.toRequestBody("application/json".toMediaTypeOrNull())
                                val request = Request.Builder().url(url).post(requestBody).build()

                                client.newCall(request).execute().use { response ->
                                    if (!response.isSuccessful) {
                                        throw IOException("Code de réponse inattendu: ${response.code}")
                                    }
                                    val responseBody = response.body?.string()
                                    responseBody?.let { JSONObject(it).optString("access_token") }
                                }
                            }

                            if (!token.isNullOrEmpty()) {
                                Log.d("LOGIN_SUCCESS", "Token reçu et sauvegardé.")
                                sessionManager.saveAuthToken(token)
                                onLoginSuccess()
                            } else {
                                Log.e("LOGIN_ERROR", "Token manquant dans la réponse.")
                                Toast.makeText(context, "Email ou mot de passe incorrect.", Toast.LENGTH_LONG).show()
                            }

                        } catch (e: Exception) {
                            Log.e("LOGIN_FAILURE", "Erreur de connexion réseau", e)
                            Toast.makeText(context, "Erreur de connexion. Vérifiez votre réseau.", Toast.LENGTH_LONG).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Se connecter")
                }
            }
        }
    }
}
