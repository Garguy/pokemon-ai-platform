package com.pokemonai.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pokemonai.client.core.PlatformTokenStorage
import com.pokemonai.client.core.TokenStorage
import com.pokemonai.client.core.sharedModule
import com.pokemonai.client.ui.PokemonAiNavHost
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun main() {
    startKoin {
        modules(
            sharedModule(serverUrl = "http://localhost:8080/graphql"),
            module { single<TokenStorage> { PlatformTokenStorage() } },
        )
    }
    application {
        Window(onCloseRequest = ::exitApplication, title = "Pokémon AI") {
            PokemonAiNavHost()
        }
    }
}
