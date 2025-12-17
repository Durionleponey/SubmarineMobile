package com.example.submarine.admin.composants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.submarine.admin.AdminUser

@Composable
fun UtilisateurSupprimeListItem(
    user: AdminUser,
    onReactivateClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(text = user.pseudo)
            Text(text = user.email, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }

        // Le bouton pour réactiver
        IconButton(onClick = {onReactivateClick(user.id)}) {
            Icon(
                imageVector = Icons.Default.Restore,
                contentDescription = "Réactiver l'utilisateur ${user.pseudo}"
            )
        }
    }
}