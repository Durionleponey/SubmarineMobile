package com.example.submarine.bio


data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<Map<String, Any>>?
)
