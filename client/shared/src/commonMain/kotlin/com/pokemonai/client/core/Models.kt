package com.pokemonai.client.core

data class AuthUser(val id: String, val email: String)

data class Question(
    val id: String,
    val text: String,
    val traitCategory: String,
    val displayOrder: Int,
)

data class AnswerInput(val questionId: String, val answerValue: Int)

data class PokemonSummary(
    val id: String,
    val name: String,
    val description: String?,
    val imageUrl: String?,
)

data class Recommendation(
    val id: String,
    val pokemon: PokemonSummary,
    val matchScore: Double,
    val rank: Int,
    val explanation: String?,
    val generatedAt: String,
)
