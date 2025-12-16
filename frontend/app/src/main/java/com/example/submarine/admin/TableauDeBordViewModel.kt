package com.example.submarine.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloException
import com.example.submarine.graphql.UsersQuery
import com.example.submarine.network.Apollo
import com.example.submarine.graphql.type.UserStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class AdminUser(
    val id: String,
    val pseudo: String,
    val email: String,
    val status: UserStatus
)

data class TableauDeBordUiState(
    val isLoading: Boolean = true,
    val users: List<AdminUser> = emptyList(),
    val error: String? = null
)

class TableauDeBordViewModel(private val apolloClient: ApolloClient) : ViewModel() {
    private val _uiState = MutableStateFlow(TableauDeBordUiState())
    val uiState: StateFlow<TableauDeBordUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }
    fun loadUsers() {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val response = apolloClient.query(UsersQuery()).execute()

                if (response.data != null && !response.hasErrors()) {
                    val serverUsers = response.data!!.users.map { user ->
                        AdminUser(
                            id = user._id,
                            pseudo = user.pseudo,
                            email = user.email,
                            status = user.status
                        )
                    }

                    _uiState.update {
                        it.copy(isLoading = false, users = serverUsers)
                    }
                } else {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Erreur GraphQL inconnue"
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                }
            } catch (e: ApolloException) {
                _uiState.update { it.copy(isLoading = false, error = "Erreur réseau : ${e.message}") }
            }
        }
    }
    fun supprimerUtilisateur(userId: String) {
        // TODO: Appeler la mutation GraphQL "deactivateUser(userId: userId)" ici
        // Pour l'instant, on se contente de rafraîchir la liste pour voir le changement
        loadUsers()
    }
    fun reactiverUtilisateur(userId: String) {
        // TODO: Appeler la mutation GraphQL "reactivateUser(userId: userId)" ici
        loadUsers()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val apolloClient = Apollo.apolloClient
                return TableauDeBordViewModel(apolloClient) as T
            }
        }
    }
}
