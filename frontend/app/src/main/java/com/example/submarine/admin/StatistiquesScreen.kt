package com.example.submarine.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.ui.theme.SubmarineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistiquesScreen(
    nombreUtilisateurs: Int
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nombre total d'utilisateurs actifs :",
                fontSize = 20.sp,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$nombreUtilisateurs",
                fontSize = 48.sp,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatistiquesScreenPreview() {
    SubmarineTheme {
        StatistiquesScreen(nombreUtilisateurs = 5)
    }
}