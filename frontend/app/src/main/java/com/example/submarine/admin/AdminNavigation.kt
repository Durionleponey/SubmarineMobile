package com.example.submarine.admin


import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

object AdminRoutes {
    const val HOME = "admin_home"
    const val USER_LIST = "admin_user_list"
    const val STATS = "admin_stats"
}

@Composable
fun AdminNavigation() {
    val navController = rememberNavController()
    val viewModel: TableauDeBordViewModel = viewModel() //utiliser pour le partage entre les écrans
    val uiState by viewModel.uiState.collectAsState() //récupère l'état de la liste utilisateurs

    NavHost(
        navController = navController,
        startDestination = AdminRoutes.HOME
    ) {
        composable(AdminRoutes.HOME) {
            AdminHomeScreen(
                onNavigateToUsers = {
                    navController.navigate(AdminRoutes.USER_LIST)
                },
                onNavigateToStats = {
                    navController.navigate(AdminRoutes.STATS)
                }
            )
        }

        // Définition de l'écran de la liste des utilisateurs
        composable(AdminRoutes.USER_LIST) {
            TableauDeBordScreen(viewModel = viewModel)
        }
        composable(AdminRoutes.STATS) {
            StatistiquesScreen(nombreUtilisateurs = uiState.users.size)
        }

    }
}