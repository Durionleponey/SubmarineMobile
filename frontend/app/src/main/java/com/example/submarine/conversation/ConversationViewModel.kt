package com.example.submarine.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConversationViewModel : ViewModel() {

    private val userPseudoRecup = UserPseudoRecup()
    private val _userPseudo = MutableStateFlow<String>("char")
    val userPseudo = _userPseudo.asStateFlow()

    /**
     * lance la recup du ID , le view model !!!
     *
     * @param userId
     */

    fun chargePseudo(userId: String) {
        viewModelScope.launch {
            val userPseudo = userPseudoRecup.fetchUser(userId)
            _userPseudo.value = userPseudo ?: "user inconnu"

        }
    }
}

