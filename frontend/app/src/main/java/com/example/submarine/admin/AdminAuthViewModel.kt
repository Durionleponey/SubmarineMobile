package com.example.submarine.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.example.submarine.MeAllQuery
import com.example.submarine.di.Apollo
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


enum class AuthStatus {
    CHECKING,
    IS_ADMIN,
    NOT_ADMIN,
    NOT_LOGGED_IN
}

class AdminAuthViewModel(private val apolloClient: ApolloClient) : ViewModel(){
    private val _authStatus = MutableStateFlow(AuthStatus.CHECKING)
    val authStatus = _authStatus.asStateFlow()

    init {
        checkAdminStatus()
    }

    private fun checkAdminStatus() {
        // Si personne n'est connecté, pas la peine d'aller plus loin
        if (TokenProvider.token == null) {
            _authStatus.value = AuthStatus.NOT_LOGGED_IN
            return
        }

        viewModelScope.launch {
            try {
                val response = apolloClient.query(MeAllQuery()).execute()

                if (response.data != null && !response.hasErrors()) {
                    // On vérifie le statut de l'utilisateur
                    val userStatus = response.data?.meAll?.status
                    if (userStatus?.name == "ADMIN") {
                        _authStatus.value = AuthStatus.IS_ADMIN
                    } else {
                        // Il est connecté, mais pas admin
                        _authStatus.value = AuthStatus.NOT_ADMIN
                    }
                } else {
                    // Erreur GraphQL
                    _authStatus.value = AuthStatus.NOT_ADMIN
                }
            } catch (e: Exception) {
                // Erreur réseau
                _authStatus.value = AuthStatus.NOT_ADMIN
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminAuthViewModel(Apollo.apolloClient) as T
            }
        }
    }
}