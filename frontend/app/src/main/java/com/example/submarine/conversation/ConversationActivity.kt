package com.example.submarine.conversation

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme

class ConversationActivity : ComponentActivity() {

    private val TAG = "ConversationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Log.d(TAG, "FLAG_SECURE activé.")

        val contactName = intent.getStringExtra("CONTACT_NAME") ?: "Contact Inconnu"

        Log.d(TAG, "Nom du contact : $contactName")

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
