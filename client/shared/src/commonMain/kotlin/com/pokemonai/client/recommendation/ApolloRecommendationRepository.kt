package com.pokemonai.client.recommendation

import com.apollographql.apollo.ApolloClient
import com.pokemonai.client.core.PokemonSummary
import com.pokemonai.client.core.Recommendation
import com.pokemonai.client.graphql.GenerateRecommendationsMutation
import com.pokemonai.client.graphql.MyRecommendationsQuery

class ApolloRecommendationRepository(private val apolloClient: ApolloClient) : RecommendationRepository {

    override suspend fun generate(): List<Recommendation> {
        val response = apolloClient.mutation(GenerateRecommendationsMutation()).execute()
        return response.dataOrThrow().generateRecommendations.map { it.toModel() }
    }

    override suspend fun getMyRecommendations(): List<Recommendation> {
        val response = apolloClient.query(MyRecommendationsQuery()).execute()
        return response.dataOrThrow().myRecommendations.map { it.toModel() }
    }

    private fun GenerateRecommendationsMutation.GenerateRecommendation.toModel() = Recommendation(
        id = id,
        pokemon = PokemonSummary(
            id = pokemon.id,
            name = pokemon.name,
            description = pokemon.description,
            imageUrl = pokemon.imageUrl,
            externalId = pokemon.externalId,
            types = pokemon.types,
            height = pokemon.height,
            weight = pokemon.weight,
            category = pokemon.category,
            hp = pokemon.hp,
            attack = pokemon.attack,
            defense = pokemon.defense,
            specialAttack = pokemon.specialAttack,
            specialDefense = pokemon.specialDefense,
            speed = pokemon.speed,
        ),
        matchScore = matchScore,
        rank = rank,
        explanation = explanation,
        generatedAt = generatedAt,
    )

    private fun MyRecommendationsQuery.MyRecommendation.toModel() = Recommendation(
        id = id,
        pokemon = PokemonSummary(
            id = pokemon.id,
            name = pokemon.name,
            description = pokemon.description,
            imageUrl = pokemon.imageUrl,
            externalId = pokemon.externalId,
            types = pokemon.types,
            height = pokemon.height,
            weight = pokemon.weight,
            category = pokemon.category,
            hp = pokemon.hp,
            attack = pokemon.attack,
            defense = pokemon.defense,
            specialAttack = pokemon.specialAttack,
            specialDefense = pokemon.specialDefense,
            speed = pokemon.speed,
        ),
        matchScore = matchScore,
        rank = rank,
        explanation = explanation,
        generatedAt = generatedAt,
    )
}
