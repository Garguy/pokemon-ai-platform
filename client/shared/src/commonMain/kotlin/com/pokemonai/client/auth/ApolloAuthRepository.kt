package com.pokemonai.client.auth

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloHttpException
import com.pokemonai.client.core.AuthUser
import com.pokemonai.client.core.TokenStorage
import com.pokemonai.client.graphql.LoginMutation
import com.pokemonai.client.graphql.RegisterMutation

class ApolloAuthRepository(
    private val apolloClient: ApolloClient,
    private val tokenStorage: TokenStorage,
) : AuthRepository {

    override suspend fun login(email: String, password: String): AuthUser {
        val response = try {
            apolloClient.mutation(LoginMutation(email, password)).execute()
        } catch (e: ApolloHttpException) {
            throw Exception("HTTP ${e.statusCode}", e)
        } catch (e: Exception) {
            throw Exception("Network error: ${e::class.simpleName}: ${e.message}", e)
        }
        println("LOGIN_DEBUG: data=${response.data}, errors=${response.errors}, exception=${response.exception}, cause=${response.exception?.cause}")
        response.exception?.let {
            val msg = if (it is ApolloHttpException) "HTTP ${it.statusCode}" else (it.message ?: it::class.simpleName ?: "Network error")
            throw Exception(msg, it)
        }
        val errors = response.errors
        if (!errors.isNullOrEmpty()) throw Exception(errors.joinToString { it.message })
        val payload = response.data?.login ?: throw Exception("No response from server — check backend is running and reachable")
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
