package com.example.submarine.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Pour Material 3 et la gestion RTL
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme


@Composable

fun ConversationScreen(
    contactName: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f)),
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(
                modifier = Modifier.background(Color.White)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ){
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )

                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleMedium
                    )                }
            }

        }
    }

}


@Preview(showBackground = true, name = "Conversation Screen Preview")
@Composable
fun ConversationScreenPreview() {

    SubmarineTheme {
        ConversationScreen(
            contactName = "John Doe",
            onNavigateBack = {}   )
    }
}
