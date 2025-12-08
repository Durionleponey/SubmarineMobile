package com.example.submarine.elec

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LcdScreen(
    lcdViewModel: LcdViewModel = viewModel()
) {
    val status by lcdViewModel.status.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(text = "Contrôle de l'écran LCD")

        Text(text = status)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { lcdViewModel.showScore() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Afficher le SCORE sur le LCD")
        }

        Button(
            onClick = { lcdViewModel.showPlayers() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Afficher le NOMBRE de joueurs sur le LCD")
        }
    }
}
