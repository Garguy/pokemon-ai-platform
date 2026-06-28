package com.pokemonai.client.core

interface TokenStorage {
    fun save(token: String)
    fun load(): String?
    fun clear()
}
