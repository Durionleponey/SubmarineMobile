package com.example.submarine.listeContact

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.submarine.ui.theme.SubmarineTheme

class AddContactActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                AddContactScreen(onBack = { finish() }) // ✅ Affiche le composable
            }
        }
    }
}
