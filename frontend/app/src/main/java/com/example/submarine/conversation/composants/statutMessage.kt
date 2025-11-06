package com.example.submarine.conversation.composants

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.submarine.R
enum class MessageStatus {
    SENDING,  // En cours d'envoi (état local avant confirmation du serveur)
    SENT,     // Envoyé au serveur (confirmé)
    RECEIVED, // Reçu par l'appareil du destinataire
    SEEN,     // Lu par le destinataire
    FAILED    // L'envoi a échoué
}

data class Message(
    val id: String,          // Id temporaire local, puis l'id du serveur
    val text: String,
    val timestamp: Long,
    val senderId: String,
    var status: MessageStatus // Le statut du message, 'var' pour pouvoir le modifier
)

@Composable
fun MessageStatusIndicator(status: MessageStatus, modifier: Modifier = Modifier) {

    // Cas spécial pour l'état "envoi en cours"
    if (status == MessageStatus.SENDING) {
        CircularProgressIndicator(
            modifier = modifier.size(14.dp),
            strokeWidth = 1.5.dp
        )
        return
    }

    val icon = when (status) {
        MessageStatus.SENT -> Icons.Default.Check
        MessageStatus.RECEIVED -> Icons.Default.DoneAll
        MessageStatus.SEEN -> Icons.Default.DoneAll
        MessageStatus.FAILED -> Icons.Default.ErrorOutline
        else -> null // Pour SENDING déjà géré
    }

    val tint = when (status) {
        MessageStatus.SEEN -> MaterialTheme.colorScheme.primary // Couleur "Vu" (souvent bleu)
        MessageStatus.FAILED -> MaterialTheme.colorScheme.error // Couleur pour l'erreur (rouge)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) // Gris pour SENT/RECEIVED
    }

    if (icon != null) {
        Icon(
            imageVector = icon,
            contentDescription = "Statut: $status",
            tint = tint,
            modifier = modifier
        )
    }
}

@Preview(showBackground = true) // showBackground = true ajoute un fond blanc pour mieux voir
@Composable
fun MessageStatusIndicatorPreview() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Ajoute de l'espace entre chaque ligne
    ) {

        // 1. Statut SENDING (En cours d'envoi)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MessageStatusIndicator(status = MessageStatus.SENDING, modifier = Modifier.size(16.dp))
            Text("  Envoi en cours...", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }

        // 2. Statut SENT (Envoyé)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MessageStatusIndicator(status = MessageStatus.SENT, modifier = Modifier.size(16.dp))
            Text("  Envoyé", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }

        // 3. Statut RECEIVED (Reçu par le contact)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MessageStatusIndicator(status = MessageStatus.RECEIVED, modifier = Modifier.size(16.dp))
            Text("  Reçu", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }

        // 4. Statut SEEN (Vu par le contact)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MessageStatusIndicator(status = MessageStatus.SEEN, modifier = Modifier.size(16.dp))
            Text("  Vu", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }

        // 5. Statut FAILED (Échec de l'envoi)
        Row(verticalAlignment = Alignment.CenterVertically) {
            MessageStatusIndicator(status = MessageStatus.FAILED, modifier = Modifier.size(16.dp))
            Text("  Échec de l'envoi", fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
        }
    }
}