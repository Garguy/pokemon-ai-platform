package com.pokemonai.client.core

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val PREFS_NAME = "pokemon_ai_secure_prefs"
private const val KEY_TOKEN = "auth_token"

class PlatformTokenStorage(context: Context) : TokenStorage {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun save(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    override fun load(): String? = prefs.getString(KEY_TOKEN, null)
    override fun clear() = prefs.edit().remove(KEY_TOKEN).apply()
}
