package com.example.submarine.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.submarine.BuildConfig

object RetrofitInstance {

    // ✅ On pointe vers la racine du serveur (pas /graphql), avec un "/" final.
    // ⚠️ BuildConfig.SERVER_IP doit exister (ex: "51.21.218.249")
    private const val BASE_URL = "http://${BuildConfig.SERVER_IP}:4000/"

    // 🔐 Intercepteur JWT
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        TokenProvider.token?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    // 🧾 Logs réseau (Logcat)
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ⚙️ Client HTTP
    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .build()

    // 🔹 API Auth (REST)
    val authApi: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(AuthApiService::class.java)
    }

    // 🔹 API GraphQL (si vous l’utilisez via Retrofit)
    val graphqlApi: GraphQLApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(GraphQLApiService::class.java)
    }
}
