package com.pokemonai.client.auth

import com.apollographql.apollo.ApolloClient
import com.pokemonai.client.core.AuthUser
import com.pokemonai.client.core.TokenStorage
import com.pokemonai.client.graphql.LoginMutation
import com.pokemonai.client.graphql.RegisterMutation

class ApolloAuthRepository(
    private val apolloClient: ApolloClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthUser {
        val response = apolloClient.mutation(LoginMutation(email, password)).execute()
        val payload = response.dataOrThrow().login
        tokenStorage.save(payload.token)
        return AuthUser(payload.user.id, payload.user.email)
    }

    override suspend fun register(email: String, password: String): AuthUser {
        val response = apolloClient.mutation(RegisterMutation(email, password)).execute()
        val payload = response.dataOrThrow().register
        tokenStorage.save(payload.token)
        return AuthUser(payload.user.id, payload.user.email)
    }
}
