package com.example.submarine.conversation

import android.util.Log

/**
 * SIMULATEUR DE BASE DE DONNÉES LOCALE (À REMPLACER PAR VOTRE DAO ROOM)
 *
 * Dans une application réelle, ce serait une interface (DAO) qui interagit
 * avec une base de données Room sécurisée pour stocker les messages en clair.
 * La clé est l'ID du message reçu du serveur.
 */
object LocalMessageStore {
    private const val TAG = "LocalMessageStore"

    // La Map simule la table de votre base de données locale
    private val plaintextMessages: MutableMap<String, String> = mutableMapOf()

    /**
     * Simule l'insertion d'un message en clair dans la DB locale.
     * @param messageId L'ID unique du message (reçu du serveur).
     * @param plaintext Le contenu du message avant chiffrement.
     */
    fun storePlaintext(messageId: String, plaintext: String) {
        plaintextMessages[messageId] = plaintext
        Log.d(TAG, "Plaintext stocké localement pour ID: $messageId")
    }

    /**
     * Simule la récupération du contenu en clair d'un message envoyé.
     * @param messageId L'ID du message à rechercher.
     * @return Le contenu en clair ou null s'il n'est pas trouvé.
     */
    fun getPlaintext(messageId: String): String? {
        return plaintextMessages[messageId].also {
            if (it == null) {
                Log.w(TAG, "Plaintext non trouvé localement pour ID: $messageId")
            }
        }
    }
}