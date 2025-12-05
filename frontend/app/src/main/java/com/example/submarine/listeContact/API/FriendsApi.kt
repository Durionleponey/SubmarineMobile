package com.example.submarine.listeContact.API

import com.example.submarine.graphql.AcceptFriendRequestMutation
import com.example.submarine.graphql.GetFriendsListQuery
import com.example.submarine.graphql.GetPendingFriendRequestsQuery
import com.example.submarine.graphql.RejectFriendRequestMutation
import com.example.submarine.listeContact.API.GraphQLClient

object FriendsApi {

    suspend fun getPendingRequests(token: String): List<GetPendingFriendRequestsQuery.PendingRequest> {
        val client = GraphQLClient.client.newBuilder()
            .addHttpHeader("Authorization", "Bearer $token")
            .build()

        val response = client.query(GetPendingFriendRequestsQuery()).execute()

        return response.data?.pendingRequests
            ?: throw Exception(response.errors?.firstOrNull()?.message)
    }

    suspend fun acceptFriendRequest(token: String, requestId: String): String {
        val client = GraphQLClient.client.newBuilder()
            .addHttpHeader("Authorization", "Bearer $token")
            .build()

        val response = client.mutation(AcceptFriendRequestMutation(requestId)).execute()

        return response.data?.acceptFriendRequest?.status
            ?: throw Exception(response.errors?.firstOrNull()?.message)
    }

    suspend fun rejectFriendRequest(token: String, requestId: String): String {
        val client = GraphQLClient.client.newBuilder()
            .addHttpHeader("Authorization", "Bearer $token")
            .build()

        val response = client.mutation(RejectFriendRequestMutation(requestId)).execute()

        return response.data?.rejectFriendRequest?.status
            ?: throw Exception(response.errors?.firstOrNull()?.message)
    }

    suspend fun getFriendsList(token: String): List<GetFriendsListQuery.FriendsList> {
        val client = GraphQLClient.client.newBuilder()
            .addHttpHeader("Authorization", "Bearer $token")
            .build()

        val response = client.query(GetFriendsListQuery()).execute()

        return response.data?.friendsList
            ?: throw Exception(response.errors?.firstOrNull()?.message)
    }

}