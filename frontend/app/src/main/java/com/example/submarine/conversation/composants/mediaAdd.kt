package com.example.submarine.conversation.composants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.ArtTrack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.submarine.ui.theme.SubmarineTheme


/**
 * @param modifier Changer la taille du logo
 */
@Composable
fun Media(
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ){
        Box(){
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "",
                tint = Color.DarkGray,
            )
            Icon(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp),
                imageVector = Icons.Default.Radar,
                contentDescription = "Ajouter un média",
                tint = Color.Gray

            )
        }


    }
}

@Preview
@Composable
fun PreviewMediaAdd() {
    SubmarineTheme {
        Media( modifier = Modifier.size(40.dp))
    }
}