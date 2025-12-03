package com.example.submarine.utils

import android.content.Context

object ContactPrefs {

    private const val PREFS_NAME = "contact_prefs"

    fun setCustomName(context: Context, userId: String, name: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("name_$userId", name).apply()
    }

    fun getCustomName(context: Context, userId: String): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("name_$userId", null)
    }

    fun setMuted(context: Context, userId: String, muted: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("mute_$userId", muted).apply()
    }

    fun isMuted(context: Context, userId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("mute_$userId", false)
    }
}
