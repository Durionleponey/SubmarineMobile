package com.example.submarine.admin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.submarine.ui.theme.SubmarineTheme

class AdminActivity : ComponentActivity() {

    private val authViewModel: AdminAuthViewModel by viewModels { AdminAuthViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                val status by authViewModel.authStatus.collectAsState()

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (status) {
                        AuthStatus.CHECKING -> {
                            // 1. En cours de vérification : on affiche une roue de chargement
                            CircularProgressIndicator()
                        }

                        AuthStatus.IS_ADMIN -> {
                            // 2. C'est un admin ! On peut afficher le vrai panel
                            val navController = rememberNavController()
                            AdminNavigation(navController = navController)
                        }

                        AuthStatus.NOT_ADMIN -> {
                            // 3. Ce n'est pas un admin : on affiche un message et on ferme
                            Toast.makeText(
                                this@AdminActivity,
                                "Accès non autorisé.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Ferme l'activité
                        }

                        AuthStatus.NOT_LOGGED_IN -> {
                            // 4. Personne n'est connecté : on affiche un message et on ferme
                            Toast.makeText(
                                this@AdminActivity,
                                "Veuillez vous connecter.",
                                Toast.LENGTH_LONG
                            ).show()
                            finish() // Ferme l'activité
                        }
                    }
                }
            }
        }
    }
}