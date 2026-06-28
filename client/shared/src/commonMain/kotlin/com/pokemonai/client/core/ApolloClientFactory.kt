package com.pokemonai.client.core

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.network.http.HttpInterceptor
import com.apollographql.apollo.network.http.HttpInterceptorChain

class ApolloClientFactory(
    private val serverUrl: String,
    private val tokenStorage: TokenStorage,
) {
    fun create(): ApolloClient = ApolloClient.Builder()
        .serverUrl(serverUrl)
        .addHttpInterceptor(AuthInterceptor(tokenStorage))
        .build()
}

private class AuthInterceptor(private val tokenStorage: TokenStorage) : HttpInterceptor {
    override suspend fun intercept(
        request: com.apollographql.apollo.api.http.HttpRequest,
        chain: HttpInterceptorChain,
    ): com.apollographql.apollo.api.http.HttpResponse {
        val token = tokenStorage.load()
        val augmented = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            request
        }
        return chain.proceed(augmented)
    }
}
