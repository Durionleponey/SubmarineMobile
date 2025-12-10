package com.example.submarine.auth

import android.content.Intent
import androidx.activity.ComponentActivity
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.submarine.listeContact.ContactsActivity
import com.example.submarine.network.LoginRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val intent = activity?.intent

    val prefillEmail = "marie@exemple.com"//intent?.getStringExtra("email") ?: ""
    val prefillPassword = "StrongPass123!"//intent?.getStringExtra("password") ?: ""

    var email by remember { mutableStateOf(prefillEmail) }
    var password by remember { mutableStateOf(prefillPassword) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "Login", style = MaterialTheme.typography.headlineMedium)

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

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = RetrofitInstance.authApi.login(LoginRequest(email.trim(), password))
                    if (response.isSuccessful) {
                        TokenProvider.token = response.body()?.token
                        Log.d("API", "Token reçu : ${TokenProvider.token}")

                        withContext(Dispatchers.Main) {
                            errorMessage = null
                            val intent = Intent(context, ContactsActivity::class.java)
                            context.startActivity(intent)
                        }
                    } else {
                        println("❌ Erreur lors de la connexion : ${response.errorBody()?.string()}")
                        Log.e("auth","❌ Erreur de connexion : ${response.code()}")
                        withContext(Dispatchers.Main) {
                            errorMessage = "Email ou mot de passe incorrect."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        OutlinedButton(
            onClick = {
                val intent = Intent(context, SignUpActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create a new account")
        }
    }
}
