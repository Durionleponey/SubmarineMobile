package com.example.submarine.admin

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

object AdminRoutes {
    const val HOME = "admin_home"
    const val USER_LIST = "admin_user_list"
    const val STATS = "admin_stats"
    const val DELETED_USERS = "admin_deleted_users"
}

@Composable
fun AdminNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewModel: TableauDeBordViewModel = viewModel() //utiliser pour le partage entre les écrans
    val uiState by viewModel.uiState.collectAsState() //récupère l'état de la liste utilisateurs

    NavHost(
        navController = navController,
        startDestination = AdminRoutes.HOME
    ) {
        composable(AdminRoutes.HOME) {
            AdminHomeScreen(
                onNavigateBack = {
                    activity?.finish()                },
                onNavigateToUsers = {
                    navController.navigate(AdminRoutes.USER_LIST)
                },
                onNavigateToStats = {
                    navController.navigate(AdminRoutes.STATS)
                },
                onNavigateToDeletedUsers = {
                    navController.navigate(AdminRoutes.DELETED_USERS)
                }
            )
        }

        // Définition de l'écran de la liste des utilisateurs
        composable(AdminRoutes.USER_LIST) {
            TableauDeBordScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AdminRoutes.STATS) {
            val totalUsers = uiState.activeUsers.size + uiState.deletedUsers.size
            StatistiquesScreen(
                nombreUtilisateursActifs = uiState.activeUsers.size,
                nombreUtilisateursSupprimes = uiState.deletedUsers.size,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AdminRoutes.DELETED_USERS) {
            ComptesSupprimesScreen(
                utilisateursSupprimes = uiState.deletedUsers,
                onNavigateBack = { navController.popBackStack() }
            )
        }

    }
}