package com.example.submarine.conversation

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.submarine.screens.ConversationScreen
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.compose.runtime.collectAsState
import androidx.activity.viewModels
import androidx.compose.runtime.getValue

class ConversationActivity : ComponentActivity() {

    private val TAG = "ConversationActivity"
    private val viewModel: ConversationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        Log.d(TAG, "FLAG_SECURE activé.")

        //val userId = intent.getStringExtra("userId") // recup de l'id qui l'a initié mais ici test
        val userId = "6910ae154e5e95f212c42612"

        if (userId == null){
            Log.e(TAG, "L'ID de l'utilisateur n'a pas été transmis.")
            finish()
            return
        }else{

            Log.d(TAG, "L'ID de l'utilisateur est : $userId")
            viewModel.chargePseudo(userId)

        }

        setContent {
            SubmarineTheme {
                val pseudo by viewModel.userPseudo.collectAsState()
                ConversationScreen(
                    contactName = pseudo?: "User test",
                    onNavigateBack = {
                        finish()
                    }
                )
            }
        }
    }
}
