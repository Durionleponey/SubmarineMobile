package com.example.submarine.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.submarine.ui.theme.SubmarineTheme

class AdminActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                val navController = rememberNavController()
                AdminNavigation(navController = navController)
            }
        }
/**   
import androidx.activity.compose.setContent
import com.example.submarine.ui.theme.SubmarineTheme

class AdminActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SubmarineTheme {
                AdminNavigation()
            }
        }
    }
    **/
}