package com.example.submarine.network

// Réponse commune renvoyée par le backend pour le LCD
data class LcdMessageResult(
    val success: Boolean,
    val message: String
)

// Structure exacte de la réponse pour la mutation sendAdminThanks
data class SendAdminThanksData(
    val sendAdminThanks: LcdMessageResult
)

// Structure exacte de la réponse pour la mutation sendAlertMessage
data class SendAlertMessageData(
    val sendAlertMessage: LcdMessageResult
)
