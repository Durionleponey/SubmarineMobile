package com.example.submarine.admin

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class AdminUser(
    val id: Int,
    val name: String
)
data class AdminState(
    val users: List<AdminUser> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TableauDeBordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AdminState())
    val uiState: StateFlow<AdminState> = _uiState.asStateFlow()

    init {
        _uiState.value = AdminState(
            users = listOf(
                AdminUser(id = 1, name = "Alice"),
                AdminUser(id = 2, name = "Bob"),
                AdminUser(id = 3, name = "Charlie"),
                AdminUser(id = 4, name = "David"),
                AdminUser(id = 5, name = "Eve")
            )
        )
    }

    fun supprimerUtilisateur(userId: Int) {
        // `update` est la manière sécurisée de modifier l'état.
        _uiState.update { currentState ->
            // On crée une nouvelle liste qui contient tous les utilisateurs SAUF celui avec l'ID correspondant.
            val nouvelleListe = currentState.users.filter { user -> user.id != userId }
            // On retourne le nouvel état avec la liste mise à jour.
            currentState.copy(users = nouvelleListe)
    }
    }

}
