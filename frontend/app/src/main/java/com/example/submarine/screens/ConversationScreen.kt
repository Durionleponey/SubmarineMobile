package com.example.submarine.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import com.example.submarine.conversation.composants.MessageBubble
import com.example.submarine.conversation.composants.IconUser
import com.example.submarine.conversation.composants.Media
import com.example.submarine.conversation.composants.PhotoSend
import com.example.submarine.conversation.composants.SendButton


data class Message(
    val text: String,
    val isSentByMe: Boolean
)

@Composable
fun ConversationScreen(
    contactName: String,
    onNavigateBack: () -> Unit
) {
    val messagesList = remember {
        mutableStateListOf(
            Message("Bonjour !", false),
            Message("Salut, comment ça va ?", true)
        )
    }
    var textState by remember { mutableStateOf("") }


    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.1f)
                )
        ) {
            Box(
                modifier = Modifier.background(Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    IconUser(modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.width(8.dp)) //espace entre l'image et le nom du contact
                    Text(
                        text = contactName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                reverseLayout = true
            ) {
                items(messagesList.asReversed()) { message ->
                    MessageBubble(message = message)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Media(modifier = Modifier.size(40.dp))
                    PhotoSend(modifier = Modifier.size(40.dp))

                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        placeholder = { Text("Écrire un message...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    SendButton(
                        isEnabled = textState.isNotBlank(),
                        onClick = {
                            if (textState.isNotBlank()) {
                                messagesList.add(Message(textState, true))
                                textState = ""
                            }
                        }
                    )

                }
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
            onNavigateBack = {}
        )
    }
}
