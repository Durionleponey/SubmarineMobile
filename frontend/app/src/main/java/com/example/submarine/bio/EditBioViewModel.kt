package com.example.submarine.bio

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch




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

    // ⚙️ Sauvegarder la bio (mutation GraphQL)
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

                // ✅ Exécution de la mutation en mode générique (Map)
                val response = RetrofitInstance.graphqlApi.executeGraphQL<Map<String, Any>>(
                    token = "Bearer $token",
                    request = request
                )

                if (response.isSuccessful && response.body()?.data != null) {
                    val dataMap = response.body()?.data
                    val updateBio = dataMap?.get("updateBio") as? Map<*, *>
                    val bioValue = updateBio?.get("bio") as? String

                    _updateStatus.value = "Bio mise à jour : ${bioValue ?: "inconnue"} ✅"
                } else {
                    val error = response.body()?.errors
                        ?.joinToString { err -> err["message"]?.toString() ?: "Erreur inconnue" }
                    _updateStatus.value = "Erreur : ${error ?: response.message()}"
                }

            } catch (e: Exception) {
                _updateStatus.value = "Erreur : ${e.localizedMessage}"
                Log.e("GraphQL", "❌ Exception : ${e.localizedMessage}", e)
            }
        }
    }


    // 🧭 Charger la bio actuelle de l’utilisateur
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

                val request = GraphQLRequest(query = query)
                Log.d("GraphQL", "✉️ Requête GraphQL : $request")

                val response = RetrofitInstance.graphqlApi.executeGraphQL<Map<String, Any>>(
                    token = "Bearer $token",
                    request = request
                )

                Log.d("GraphQL", "📡 Code HTTP = ${response.code()}")
                Log.d("GraphQL", "🧾 Body = ${response.body()}")
                Log.d("GraphQL", "❌ ErrorBody = ${response.errorBody()?.string()}")

                if (response.isSuccessful && response.body()?.data != null) {
                    val dataMap = response.body()?.data as? Map<*, *>
                    val userBio = dataMap?.get("getBio") as? String ?: ""

                    Log.d("GraphQL", "📥 Bio reçue du serveur : $userBio")
                    bio.value = userBio
                    _updateStatus.value = "Bio chargée avec succès ✅"
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
