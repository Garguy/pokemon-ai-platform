package com.pokemonai.client.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pokemonai.client.core.SelectedRecommendationHolder
import com.pokemonai.client.core.TokenStorage
import com.pokemonai.client.questionnaire.QuestionnaireRepository
import org.koin.compose.koinInject

object Route {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val QUESTIONNAIRE = "questionnaire"
    const val PROCESSING = "processing"
    const val RESULT = "result"
    const val DETAIL = "detail"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
}

@Composable
fun PokemonAiNavHost() {
    val navController = rememberNavController()
    val tokenStorage = koinInject<TokenStorage>()
    val selectionHolder = koinInject<SelectedRecommendationHolder>()
    val questionnaireRepo = koinInject<QuestionnaireRepository>()

    val systemDark = isSystemInDarkTheme()
    var themeOverride by remember { mutableStateOf<Boolean?>(null) }
    val dark = themeOverride ?: systemDark

    // Shallow, consistent tab navigation: RESULT stays the base, with at most one
    // secondary tab (History/Settings) stacked on top.
    val goMatches = {
        navController.navigate(Route.RESULT) {
            popUpTo(Route.RESULT) { inclusive = true }
            launchSingleTop = true
        }
    }
    val goHistory = {
        navController.navigate(Route.HISTORY) {
            popUpTo(Route.RESULT)
            launchSingleTop = true
        }
    }
    val goSettings = {
        navController.navigate(Route.SETTINGS) {
            popUpTo(Route.RESULT)
            launchSingleTop = true
        }
    }
    val goRetake = { navController.navigate(Route.QUESTIONNAIRE); Unit }
    val signOut = {
        tokenStorage.clear()
        navController.navigate(Route.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    AppTheme(dark = dark, toggleTheme = { themeOverride = !dark }) {
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
            ResultScreen(
                onSelectRecommendation = { rec ->
                    selectionHolder.selected = rec
                    navController.navigate(Route.DETAIL)
                },
                onTakeQuiz = goRetake,
                onViewHistory = goHistory,
                onSettings = goSettings,
                onSignOut = signOut,
            )
        }
        composable(Route.DETAIL) {
            // Load profile into observable state so the screen recomposes once it arrives.
            var profile by remember { mutableStateOf(selectionHolder.profile) }
            LaunchedEffect(Unit) {
                if (profile == null) {
                    val loaded = try { questionnaireRepo.getProfile() } catch (_: Exception) { null }
                    selectionHolder.profile = loaded
                    profile = loaded
                }
            }
            val rec = selectionHolder.selected
            if (rec != null) {
                PokemonDetailScreen(
                    recommendation = rec,
                    profile = profile,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Route.HISTORY) {
            HistoryScreen(
                onMatches = goMatches,
                onRetake = goRetake,
                onSettings = goSettings,
            )
        }
        composable(Route.SETTINGS) {
            SettingsScreen(
                onMatches = goMatches,
                onHistory = goHistory,
                onRetake = goRetake,
                onSignOut = signOut,
            )
        }
        }
    }
}
