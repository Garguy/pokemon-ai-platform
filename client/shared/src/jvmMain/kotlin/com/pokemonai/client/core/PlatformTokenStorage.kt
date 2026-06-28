package com.pokemonai.client.core

import java.util.prefs.Preferences

class PlatformTokenStorage : TokenStorage {
    private val prefs = Preferences.userRoot().node("com/pokemonai/client")
    private val key = "auth_token"

    override fun save(token: String) = prefs.put(key, token)
    override fun load(): String? = prefs.get(key, null)
    override fun clear() = prefs.remove(key)
}
