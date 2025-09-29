package com.example.submarine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import com.example.submarine.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubmarineTheme {
                MainScreen()
                AppNavigation()
            }
        }
    }
}

@Composable
fun MainScreen(
    onNavigateToConversation: () -> Unit = {}
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Submarine ⚠️⚠️⚠️")

            Button(onClick = onNavigateToConversation) {
                Text(text = "Go to Conversation")
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SubmarineTheme {
        MainScreen()
    }
}
