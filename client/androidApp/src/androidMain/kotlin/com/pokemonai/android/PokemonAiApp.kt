package com.pokemonai.android

import android.app.Application
import com.pokemonai.client.core.PlatformTokenStorage
import com.pokemonai.client.core.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PokemonAiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@PokemonAiApp)
            modules(
                sharedModule(serverUrl = "http://10.0.2.2:8080/graphql"),
                module { single { PlatformTokenStorage(androidContext()) } },
            )
        }
    }
}
