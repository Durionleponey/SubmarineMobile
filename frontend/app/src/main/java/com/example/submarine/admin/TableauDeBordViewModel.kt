package com.example.submarine.admin

import android.util.Log
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
import com.example.submarine.graphql.DeactivateUserMutation
import com.example.submarine.graphql.ReactivateUserMutation
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
        viewModelScope.launch {
            try {
                val response = apolloClient.mutation(DeactivateUserMutation(userId = userId)).execute()

                if (response.data != null && !response.hasErrors()) {
                    val newStatus = response.data?.deactivateUser?.status

                    Log.d("ViewModel", "Utilisateur $userId désactivé. Nouveau statut: $newStatus")

                    _uiState.update { currentState ->
                        val updatedUsers = currentState.users.map { user ->
                            if (user.id == userId) {
                                user.copy(status = newStatus ?: UserStatus.DELETED)
                            } else {
                                user
                            }
                        }
                        currentState.copy(users = updatedUsers)
                    }
                } else {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Erreur lors de la désactivation"
                    Log.e("ViewModel", "Erreur GraphQL: $errorMessage")
                    _uiState.update { it.copy(error = errorMessage) }
                }

            } catch (e: ApolloException) {
                // Erreur réseau
                Log.e("ViewModel", "Erreur réseau lors de la désactivation: ${e.message}")
                _uiState.update { it.copy(error = "Erreur réseau: ${e.message}") }
            }
        }
    }
    fun reactiverUtilisateur(userId: String) {
        Log.d("REAC_TEST", "!!!!!!!!!!!!!! LA FONCTION A ÉTÉ APPELÉE !!!!!!!!!!!!!! ID: $userId")
        viewModelScope.launch {
            try {
                val response = apolloClient.mutation(ReactivateUserMutation(userId = userId)).execute()

                if (response.data != null && !response.hasErrors()) {
                    val newStatus = response.data?.reactivateUser?.status
                    Log.d("ViewModel", "Utilisateur $userId réactivé. Nouveau statut: $newStatus")

                    _uiState.update { currentState ->
                        val updatedUsers = currentState.users.map { user ->
                            if (user.id == userId) {
                                // On met à jour le statut de l'utilisateur concerné
                                user.copy(status = newStatus ?: UserStatus.ACTIVE)
                            } else {
                                user
                            }
                        }
                        currentState.copy(users = updatedUsers)
                    }
                } else {
                    val errorMessage = response.errors?.firstOrNull()?.message ?: "Erreur lors de la réactivation"
                    Log.e("ViewModel", "Erreur GraphQL: $errorMessage")
                    _uiState.update { it.copy(error = errorMessage) }
                }
            } catch (e: ApolloException) {
                Log.e("ViewModel", "Erreur réseau lors de la réactivation: ${e.message}")
                _uiState.update { it.copy(error = "Erreur réseau: ${e.message}") }
            }
        }
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
