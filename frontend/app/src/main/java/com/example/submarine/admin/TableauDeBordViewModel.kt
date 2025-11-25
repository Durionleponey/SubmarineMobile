package com.example.submarine.admin

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdminUser(
    val id: Int,
    val name: String
)

data class TableauDeBordUiState(
    val users: List<AdminUser> = emptyList()
)

class TableauDeBordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TableauDeBordUiState())
    val uiState: StateFlow<TableauDeBordUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = TableauDeBordUiState(
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
        _uiState.update { currentState ->
            val nouvelleListe = currentState.users.filter { user -> user.id != userId }
            currentState.copy(users = nouvelleListe)
        }
    }
}
