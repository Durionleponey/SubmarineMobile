package com.example.submarine.elec

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.submarine.R
import com.example.submarine.network.GraphQLApiService
import com.example.submarine.network.GraphQLRequest
import com.example.submarine.network.RetrofitInstance
import com.example.submarine.network.TokenProvider
import kotlinx.coroutines.launch

class LcdActivity : ComponentActivity() {

    // Service GraphQL
    private val graphQLApi: GraphQLApiService by lazy {
        // attention au nom de la propriété dans RetrofitInstance :
        RetrofitInstance.graphqlApi
    }

    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lcd)

        tvStatus = findViewById(R.id.tvLcdStatus)

        // IDs = ceux de activity_lcd.xml
        val btnThankAdmin: Button = findViewById(R.id.btnLcdShowIp)
        val btnAlertMessage: Button = findViewById(R.id.btnLcdShowMessage)

        btnThankAdmin.setOnClickListener {
            lifecycleScope.launch {
                sendAdminThanks()
            }
        }

        btnAlertMessage.setOnClickListener {
            lifecycleScope.launch {
                sendAlertMessage()
            }
        }
    }

    // -----------------------------
    //   Appel GraphQL : merci admin
    // -----------------------------
    private suspend fun sendAdminThanks() {
        val query = """
            mutation SendAdminThanks {
              sendAdminThanks {
                success
                message
              }
            }
        """.trimIndent()

        val request = GraphQLRequest(query = query)
        val tokenHeader = TokenProvider.token?.let { "Bearer $it" } ?: ""

        try {
            // ⚠️ On précise le type générique : Any
            val response = graphQLApi.executeGraphQL<Any>(
                tokenHeader,
                request
            )

            runOnUiThread {
                if (response.isSuccessful) {
                    val msg = "Message de remerciement envoyé à l'administrateur."
                    tvStatus.text = msg
                    Toast.makeText(this@LcdActivity, msg, Toast.LENGTH_LONG).show()
                } else {
                    val msg = "Erreur réseau : ${response.code()}"
                    tvStatus.text = msg
                    Toast.makeText(this@LcdActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                val msg = "Erreur : ${e.localizedMessage ?: "inconnue"}"
                tvStatus.text = msg
                Toast.makeText(this@LcdActivity, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    // -----------------------------
    //   Appel GraphQL : alerte
    // -----------------------------
    private suspend fun sendAlertMessage() {
        val query = """
            mutation SendAlertMessage {
              sendAlertMessage {
                success
                message
              }
            }
        """.trimIndent()

        val request = GraphQLRequest(query = query)
        val tokenHeader = TokenProvider.token?.let { "Bearer $it" } ?: ""

        try {
            val response = graphQLApi.executeGraphQL<Any>(
                tokenHeader,
                request
            )

            runOnUiThread {
                if (response.isSuccessful) {
                    // 👉 Message en haut de la page
                    val uiMsg =
                        "Une alerte a été envoyée à l'administrateur.\n" +
                                "Veuillez le contacter au plus vite via submarinehelpdesk@gmail.com."
                    tvStatus.text = uiMsg

                    Toast.makeText(
                        this@LcdActivity,
                        "Alerte envoyée à l'administrateur.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val msg = "Erreur réseau : ${response.code()}"
                    tvStatus.text = msg
                    Toast.makeText(this@LcdActivity, msg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                val msg = "Erreur : ${e.localizedMessage ?: "inconnue"}"
                tvStatus.text = msg
                Toast.makeText(this@LcdActivity, msg, Toast.LENGTH_LONG).show()
            }
        }
    }
}
