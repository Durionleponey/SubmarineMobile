package com.example.submarine.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Crée un fichier de préférences privé pour l'application
    private val prefs: SharedPreferences = context.getSharedPreferences("submarine_prefs", Context.MODE_PRIVATE)

    // 'companion object' est similaire à des membres statiques en Java
    companion object {
        // La clé pour stocker le token.
        const val AUTH_TOKEN = "auth_token"
    }

    /**
     * Sauvegarde le token d'authentification dans les préférences.
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply() // 'apply' sauvegarde les données en arrière-plan
    }

    /**
     * Récupère le token d'authentification depuis les préférences.
     * Renvoie 'null' si aucun token n'est trouvé.
     */
    fun getAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    /**
     * Efface le token d'authentification (utile pour la déconnexion).
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }
}
