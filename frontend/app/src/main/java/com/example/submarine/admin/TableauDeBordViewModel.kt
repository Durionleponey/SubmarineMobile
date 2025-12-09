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
    val activeUsers: List<AdminUser> = emptyList(),
    val deletedUsers: List<AdminUser> = emptyList()
)

class TableauDeBordViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TableauDeBordUiState())
    val uiState: StateFlow<TableauDeBordUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = TableauDeBordUiState(
            activeUsers = listOf(
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
            val userToMove = currentState.activeUsers.find { it.id == userId }

            if (userToMove != null) {
                // On le retire de la liste des actifs
                val newActiveList = currentState.activeUsers.filter { it.id != userId }
                // On l'ajoute à la liste des supprimés
                val newDeletedList = currentState.deletedUsers + userToMove

                // On retourne le nouvel état avec les deux listes mises à jour
                currentState.copy(
                    activeUsers = newActiveList,
                    deletedUsers = newDeletedList
                )
            } else {
                currentState
            }
        }
    }
    fun reactiverUtilisateur(userId: Int) {
        _uiState.update { currentState ->
            val userToMove = currentState.deletedUsers.find { it.id == userId }
            if (userToMove != null) {
                currentState.copy(
                    deletedUsers = currentState.deletedUsers.filterNot { it.id == userId },
                    activeUsers = (currentState.activeUsers + userToMove).sortedBy { it.name }
                )
            } else {
                currentState
            }
        }
    }
}
