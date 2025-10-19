package com.example.submarine.conversation.composants

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.submarine.ui.theme.SubmarineTheme

/**
 * Boutton pour envoyer un message.
 *
 * @param onClick Fonction à exécuter lorsque le bouton est cliqué.
 * @param isEnabled Indique si le bouton est activable ou non.
 */

@Composable
fun SendButton(
    isEnabled: Boolean,
    onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Envoyer",
            tint = if (isEnabled) Color.DarkGray else Color.Gray
        )
    }
}


@Preview
@Composable
fun PreviewSendEnabled(){
    SubmarineTheme {
        SendButton(isEnabled = true, onClick = {})
    }
}

@Preview
@Composable
fun PreviewSendDisabled(){
    SubmarineTheme {
        SendButton(isEnabled = false, onClick = {})
    }
}