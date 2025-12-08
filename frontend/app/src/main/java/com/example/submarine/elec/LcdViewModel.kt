package com.example.submarine.elec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LcdViewModel(
    private val controller: LcdController = LcdController()
) : ViewModel() {

    private val _status = MutableStateFlow("LCD en attente…")
    val status: StateFlow<String> = _status

    fun showScore() {
        sendToLcd("SHOW_SCORE", "Affichage du score sur le LCD…")
    }

    fun showPlayers() {
        sendToLcd("SHOW_PLAYERS", "Affichage du nombre de joueurs sur le LCD…")
    }

    private fun sendToLcd(command: String, message: String) {
        viewModelScope.launch {
            _status.value = message
            controller.sendCommand(command)
        }
    }
}
