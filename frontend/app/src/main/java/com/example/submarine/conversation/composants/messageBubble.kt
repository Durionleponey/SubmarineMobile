package com.example.submarine.conversation.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.screens.Message
import com.example.submarine.ui.theme.SubmarineTheme

@Composable
fun MessageBubble(
    message: String,
    isSentByMe: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSentByMe) Color.DarkGray else Color.LightGray)
        ) {
            Text(
                text = message,
                color = if (isSentByMe) Color.White else Color.Black,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}


@Preview(name = "Bulle de Message Reçu", showBackground = true)
@Composable
fun PreviewMessageBubbleReceived() {
    SubmarineTheme {
        MessageBubble(
            message = "Salut ! Ceci est un message reçu.",
            isSentByMe = false
        )
    }
}

@Preview(name = "Bulle de Message Envoyé", showBackground = true)
@Composable
fun PreviewMessageBubbleSent() {
    SubmarineTheme {
        MessageBubble(
            message = "Salut ! Ceci est un message reçu.",
            isSentByMe = true
        )
    }
}