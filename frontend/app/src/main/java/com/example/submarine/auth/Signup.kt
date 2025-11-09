package com.example.submarine.auth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme
import androidx.compose.ui.platform.LocalContext // Important
import com.example.submarine.bio.EditBioActivity // Important
import android.content.Intent
import com.example.submarine.MainActivity
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.LoginRequest
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext




class Signup : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SubmarineTheme {
                SignupScreen()
            }
        }
    }
}

//this is juste for the preview
@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    SubmarineTheme {
        SignupScreen()
    }
}

