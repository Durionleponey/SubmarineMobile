package com.example.submarine.bio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.submarine.network.TokenProvider
import android.util.Log

class EditBioViewModel : ViewModel() {

    // ✅ Données observables
    val username = MutableStateFlow("Bio")
    val bio = MutableStateFlow("")


    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus = _updateStatus.asStateFlow()

    // ⚙️ Gérer la saisie
    fun onBioChange(newBio: String) {
        bio.value = newBio
    }

    // ⚙️ Sauvegarder (mutation GraphQL)
    fun saveBio() {
        val token = TokenProvider.token

        if (token.isNullOrEmpty()) {
            _updateStatus.value = "Erreur : non authentifié"
            return
        }

        viewModelScope.launch {
            try {
                val mutation = """
                    mutation UpdateBio(${'$'}bio: String!) {
                      updateBio(updateUserBio: { bio: ${'$'}bio }) {
                        _id
                        bio
                      }
                    }
                """.trimIndent()

                val request = GraphQLRequest(
                    query = mutation,
                    variables = mapOf("bio" to bio.value)
                )

                val response = RetrofitInstance.api.updateBio(
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful && response.body()?.data != null) {
                    _updateStatus.value = "Bio mise à jour avec succès !"
                } else {
                    val error = response.body()?.errors?.joinToString { it["message"].toString() }
                    _updateStatus.value = "Erreur : ${error ?: response.message()}"
                }
            } catch (e: Exception) {
                _updateStatus.value = "Erreur : ${e.localizedMessage}"
            }
        }
    }

    // 🧭 Charger la bio de l’utilisateur connecté
    fun loadUserBio() {
        Log.d("GraphQL", "🚀 loadUserBio() appelée")
        val token = TokenProvider.token

        if (token.isNullOrEmpty()) {
            _updateStatus.value = "Erreur : non authentifié"
            return
        }

        viewModelScope.launch {
            try {
                val query = """
                    
                      query { getBio }
                    

                """.trimIndent()
                Log.d("GraphQL", "✉️ Requête GraphQL : $query")


                val request = GraphQLRequest(query = query)
                Log.d("GraphQL", "✉️ Requête GraphQL : $request")


                val response = RetrofitInstance.api.queryGraphQL(
                    token = "Bearer $token",
                    request = request
                )
                Log.d("GraphQL", "✉️ Requête GraphQL : $response")


                Log.d("GraphQL", "📡 Code HTTP = ${response.code()}")
                Log.d("GraphQL", "🧾 Body = ${response.body()}")
                Log.d("GraphQL", "❌ ErrorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body()?.data != null) {
                    val dataMap = response.body()?.data as? Map<*, *>
                    val userBio = dataMap?.get("getBio") as? String ?: ""

                    Log.d("GraphQL", "📥 Bio reçue du serveur : $userBio")

                    bio.value = userBio
                    _updateStatus.value = "Bio chargée avec succès."
                } else {
                    val error = response.body()?.errors?.joinToString { it["message"].toString() }
                    _updateStatus.value = "Erreur : ${error ?: response.message()}"
                }

            } catch (e: Exception) {
                _updateStatus.value = "Erreur : ${e.localizedMessage}"
                Log.e("GraphQL", "❌ Exception : ${e.localizedMessage}", e)
            }
        }
    }
}
