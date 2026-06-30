package com.pokemonai.client.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.pokemonai.client.ui.PokemonAiNavHost
import com.pokemonai.client.ui.PokemonTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.module
import platform.UIKit.UIViewController

private const val SERVER_URL = "http://localhost:8080/graphql"

@OptIn(ExperimentalComposeUiApi::class)
fun mainViewController(): UIViewController = ComposeUIViewController(configure = {
    enforceStrictPlistSanityCheck = false
    opaque = true
}) {
    KoinApplication(application = {
        modules(
            sharedModule(serverUrl = SERVER_URL),
            module { single<TokenStorage> { PlatformTokenStorage() } },
        )
    }) {
        PokemonTheme {
            Box(modifier = Modifier.fillMaxSize()) {
                PokemonAiNavHost()
            }
        }
    }
}
