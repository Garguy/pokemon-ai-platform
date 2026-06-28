package com.pokemonai.client.recommendation

import com.pokemonai.client.core.Recommendation

interface RecommendationRepository {
    suspend fun generate(): List<Recommendation>
    suspend fun getMyRecommendations(): List<Recommendation>
}
