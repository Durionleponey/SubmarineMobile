package com.example.submarine.admin

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object AdminRoutes {
    const val HOME = "admin_home"
    const val USER_LIST = "admin_user_list"
}

@Composable
fun AdminNavigation() {
    // Le NavController se souvient de la pile d'écrans et gère la navigation.
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AdminRoutes.HOME
    ) {
        // Définition de l'écran d'accueil
        composable(AdminRoutes.HOME) {
            AdminHomeScreen(
                onNavigateToUsers = {
                    navController.navigate(AdminRoutes.USER_LIST)
                }
            )
        }

        // Définition de l'écran de la liste des utilisateurs
        composable(AdminRoutes.USER_LIST) {
            TableauDeBordScreen()
        }

        // On ajoutera les autres écrans (statistiques, etc.) ici plus tard
    }
}