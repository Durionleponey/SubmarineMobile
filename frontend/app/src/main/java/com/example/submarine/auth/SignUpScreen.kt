package com.example.submarine.auth

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import com.example.submarine.contacts.ContactsActivity
import com.example.submarine.network.LoginRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignupScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

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
                        message = "❌ Error creating account: invalid email or password is not secure enough"

                        Log.e("auth","❌ Erreur de connexion : ${response.code()}")
                        withContext(Dispatchers.Main) {
                            errorMessage = "Email ou mot de passe incorrect."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GO!")
        }
    }
}