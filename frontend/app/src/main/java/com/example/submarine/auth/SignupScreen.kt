package com.example.submarine.auth

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.submarine.bio.EditBioActivity
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.LoginRequest
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SignupScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = RetrofitInstance.authApi.login(LoginRequest(email, password))
                    if (response.isSuccessful) {
                        TokenProvider.token = response.body()?.token
                        println("✅ Token reçu : ${TokenProvider.token}")

                        // 🟢 Redirige vers EditBioActivity après connexion réussie
                        withContext(Dispatchers.Main) {
                            val intent = Intent(context, EditBioActivity::class.java)
                            context.startActivity(intent)
                        }
                    } else {
                        println("❌ Erreur de connexion : ${response.code()}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("GO!")
        }
    }
}
