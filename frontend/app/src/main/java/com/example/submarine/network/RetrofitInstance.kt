package com.example.submarine.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    // 🧩 Adresse de ton backend (Android Emulator → localhost)
    private const val BASE_URL = "http://-/"

    // 🔐 Ajoute automatiquement le token JWT dans chaque requête si présent
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        TokenProvider.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    // 🧾 Intercepteur pour afficher les requêtes/réponses dans Logcat
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ⚙️ Configuration du client HTTP commun
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    // 🔹 Instance Retrofit pour les appels REST (authentification, etc.)
    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AuthApiService::class.java)
    }

    // 🔹 Instance Retrofit pour les appels GraphQL génériques (bio, pseudo, etc.)
    val graphqlApi: GraphQLApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GraphQLApiService::class.java)
    }
}
