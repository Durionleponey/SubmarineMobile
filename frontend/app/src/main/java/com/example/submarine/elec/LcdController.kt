package com.example.submarine.elec

import android.util.Log

/**
 * Ici on isolera TOUT ce qui parle vraiment au matériel (ESP32, Arduino, etc.).
 * Pour l’instant c’est juste un stub qui logge ce qu’on lui demande.
 */
class LcdController {

    fun sendCommand(command: String) {
        // TODO: remplacer ça plus tard par l'envoi réel vers la carte
        Log.d("LcdController", "Commande envoyée au LCD : $command")
    }
}
