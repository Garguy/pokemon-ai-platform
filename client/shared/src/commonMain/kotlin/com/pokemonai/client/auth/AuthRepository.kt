package com.pokemonai.client.auth

import com.pokemonai.client.core.AuthUser

interface AuthRepository {
    suspend fun login(email: String, password: String): AuthUser
    suspend fun register(email: String, password: String): AuthUser
}
