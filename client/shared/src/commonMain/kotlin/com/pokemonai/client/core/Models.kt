package com.pokemonai.client.core

class SelectedRecommendationHolder {
    var selected: Recommendation? = null
    var profile: PersonalityProfile? = null
}

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
    val externalId: Int? = null,
    val types: List<String> = emptyList(),
    val height: Int? = null,
    val weight: Int? = null,
    val category: String? = null,
    val hp: Int? = null,
    val attack: Int? = null,
    val defense: Int? = null,
    val specialAttack: Int? = null,
    val specialDefense: Int? = null,
    val speed: Int? = null,
)

data class Recommendation(
    val id: String,
    val pokemon: PokemonSummary,
    val matchScore: Double,
    val rank: Int,
    val explanation: String?,
    val generatedAt: String,
)

data class PersonalityProfile(
    val energy: Double,
    val curiosity: Double,
    val leadership: Double,
    val loyalty: Double,
    val risk: Double,
    val creativity: Double,
)
