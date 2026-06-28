package com.pokemonai.client.core

import com.pokemonai.client.auth.ApolloAuthRepository
import com.pokemonai.client.auth.AuthRepository
import com.pokemonai.client.auth.LoginViewModel
import com.pokemonai.client.questionnaire.ApolloQuestionnaireRepository
import com.pokemonai.client.questionnaire.QuestionnaireRepository
import com.pokemonai.client.questionnaire.QuestionnaireViewModel
import com.pokemonai.client.recommendation.ApolloRecommendationRepository
import com.pokemonai.client.recommendation.RecommendationRepository
import com.pokemonai.client.recommendation.RecommendationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

fun sharedModule(serverUrl: String): Module = module {
    single { ApolloClientFactory(serverUrl, get()).create() }

    single<AuthRepository> { ApolloAuthRepository(get(), get()) }
    single<QuestionnaireRepository> { ApolloQuestionnaireRepository(get()) }
    single<RecommendationRepository> { ApolloRecommendationRepository(get()) }

    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.Default) }

    // Holds the currently-selected recommendation for the detail screen
    single { SelectedRecommendationHolder() }

    factory { LoginViewModel(get(), get()) }
    factory { QuestionnaireViewModel(get(), get()) }
    factory { RecommendationViewModel(get(), get()) }
}
