package com.example.submarine.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.submarine.screens.ConversationScreen
import com.example.submarine.MainScreen



object AppRoutes {
    const val HOME = "home"
    const val CONTACT = "contact"
}

@Composable

fun AppNavigation() {
    val navController = rememberNavController()
    NavHost( navController, startDestination = AppRoutes.HOME) {
        composable(AppRoutes.HOME){
            MainScreen(
                onNavigateToConversation = {
                    navController.navigate(AppRoutes.CONTACT)
                }
            )
        }
        composable(route = AppRoutes.CONTACT) {
            ConversationScreen(
                onNavigateBack = {
                    navController.navigate(AppRoutes.HOME)
                }
            )
        }
    }
}




