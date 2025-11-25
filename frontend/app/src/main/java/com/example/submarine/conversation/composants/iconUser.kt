package com.example.submarine.conversation.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IconUser(
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Gray),
        contentAlignment = Alignment.Center
    ){
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Icon User",
            tint = Color.White
        )
    }
}

@Preview(name = "IconUser taille par défaut")
@Composable
fun PreviewIconUser() {
    IconUser(modifier = Modifier.size(40.dp))
}
@Preview(name = "IconUser taille 80dp")
@Composable
fun PreviewIconUser80dp() {
    IconUser(modifier = Modifier.size(100.dp))
}