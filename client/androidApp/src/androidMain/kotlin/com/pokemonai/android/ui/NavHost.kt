package com.pokemonai.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pokemonai.client.core.TokenStorage
import org.koin.compose.koinInject

object Route {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val QUESTIONNAIRE = "questionnaire"
    const val PROCESSING = "processing"
    const val RESULT = "result"
    const val HISTORY = "history"
}

@Composable
fun PokemonAiNavHost() {
    val navController = rememberNavController()
    val tokenStorage = koinInject<TokenStorage>()

    NavHost(navController = navController, startDestination = Route.SPLASH) {
        composable(Route.SPLASH) {
            LaunchedEffect(Unit) {
                val dest = if (tokenStorage.load() != null) Route.RESULT else Route.LOGIN
                navController.navigate(dest) {
                    popUpTo(Route.SPLASH) { inclusive = true }
                }
            }
        }
        composable(Route.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Route.QUESTIONNAIRE) {
                    popUpTo(Route.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Route.QUESTIONNAIRE) {
            QuestionnaireScreen(onSubmitted = {
                navController.navigate(Route.PROCESSING)
            })
        }
        composable(Route.PROCESSING) {
            ProcessingScreen(onGenerated = {
                navController.navigate(Route.RESULT) {
                    popUpTo(Route.PROCESSING) { inclusive = true }
                }
            })
        }
        composable(Route.RESULT) {
            ResultScreen(onViewHistory = { navController.navigate(Route.HISTORY) })
        }
        composable(Route.HISTORY) {
            HistoryScreen()
        }
    }
}
