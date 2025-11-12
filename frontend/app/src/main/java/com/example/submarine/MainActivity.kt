package com.example.submarine

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.auth.Signup
import com.example.submarine.contacts.ContactsActivity
import com.example.submarine.conversation.ConversationActivity
//import com.example.submarine.auth.Signup
import com.example.submarine.ui.theme.SubmarineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubmarineTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = "Submarine ⚠️⚠️⚠️")

            Button(onClick = {
                val intent = Intent(context, Signup::class.java)
                context.startActivity(intent)
            }) {
                Text("go")
            }
            Button(onClick = {
                val intent = Intent(context, ConversationActivity ::class.java)
                context.startActivity(intent)
            }) {
                Text("Conversation")
            }


            Button(onClick = {
                val intent = Intent(context, ContactsActivity::class.java)
                context.startActivity(intent)
            }) {
                Text("Voir mes contacts")
            }

            Button(onClick = {
                val intent = Intent(context, )
                context.startActivity(intent)
            }) {
                Text("Administrateur")
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SubmarineTheme {
        MainScreen()
    }
}