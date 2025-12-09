package com.example.submarine.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.submarine.ui.theme.SubmarineTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatistiquesScreen(
    nombreUtilisateursActifs: Int,
    nombreUtilisateursSupprimes: Int,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiques") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Statistiques des Utilisateurs",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Statistique des utilisateurs actifs
                    StatItem("Utilisateurs Actifs", nombreUtilisateursActifs.toString())

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statistique des utilisateurs supprimés
                    StatItem("Utilisateurs Supprimés", nombreUtilisateursSupprimes.toString())

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Statistique du total
                    StatItem("Nombre Total d'Utilisateurs sur l'application", (nombreUtilisateursActifs + nombreUtilisateursSupprimes).toString(), isTotal = true)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, isTotal: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            fontSize = if (isTotal) 18.sp else 16.sp,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = if (isTotal) 20.sp else 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatistiquesScreenPreview() {
    SubmarineTheme {
        StatistiquesScreen(
            nombreUtilisateursActifs = 15,
            nombreUtilisateursSupprimes = 3,
            onNavigateBack = {}
        )
    }
}