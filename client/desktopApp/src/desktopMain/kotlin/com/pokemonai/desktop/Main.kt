package com.pokemonai.desktop

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
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
        val windowState = rememberWindowState(size = DpSize(520.dp, 700.dp))
        Window(
            onCloseRequest = ::exitApplication,
            title = "Pokémon AI",
            state = windowState,
        ) {
            com.pokemonai.client.ui.PokemonTheme {
                PokemonAiNavHost()
            }
        }
    }
}
