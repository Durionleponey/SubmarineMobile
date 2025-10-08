package com.example.submarine.conversation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme

class ConversationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        val contactName = intent.getStringExtra("CONTACT_NAME") ?: "Contact Inconnu"

        setContent {
            SubmarineTheme {
                ConversationScreen(
                    contactName = contactName,
                    onNavigateBack = {
                        finish()
                    }
                )
            }
        }
    }
}
