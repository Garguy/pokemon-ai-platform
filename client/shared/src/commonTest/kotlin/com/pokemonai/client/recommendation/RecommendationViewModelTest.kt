package com.pokemonai.client.recommendation

import com.pokemonai.client.core.PokemonSummary
import com.pokemonai.client.core.Recommendation
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class RecommendationViewModelTest {

    private fun pokemon(name: String) = PokemonSummary("id-$name", name, null, null)

    private val unsortedRecommendations = listOf(
        Recommendation("r3", pokemon("Snorlax"), 0.72, rank = 3, null, "2024-01-01"),
        Recommendation("r1", pokemon("Pikachu"), 0.95, rank = 1, "Energetic match!", "2024-01-01"),
        Recommendation("r2", pokemon("Eevee"), 0.88, rank = 2, null, "2024-01-01"),
    )

    @Test
    fun `load exposes recommendations sorted by rank`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeRecommendationRepository(myRecs = unsortedRecommendations)
        val vm = RecommendationViewModel(repo, this)

        vm.load()

        assertIs<UiState.Success<List<Recommendation>>>(vm.state.value)
        val sorted = (vm.state.value as UiState.Success).data
        assertEquals(listOf(1, 2, 3), sorted.map { it.rank })
        assertEquals("Pikachu", sorted[0].pokemon.name)
    }

    @Test
    fun `generate exposes recommendations sorted by rank`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeRecommendationRepository(generated = unsortedRecommendations)
        val vm = RecommendationViewModel(repo, this)

        vm.generate()

        assertIs<UiState.Success<List<Recommendation>>>(vm.state.value)
        val sorted = (vm.state.value as UiState.Success).data
        assertEquals(listOf(1, 2, 3), sorted.map { it.rank })
    }

    @Test
    fun `load transitions Loading then Error on failure`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeRecommendationRepository(loadFailure = RuntimeException("No connection"))
        val vm = RecommendationViewModel(repo, this)

        vm.load()

        assertIs<UiState.Error>(vm.state.value)
        assertEquals("No connection", (vm.state.value as UiState.Error).message)
    }
}

private class FakeRecommendationRepository(
    private val myRecs: List<Recommendation> = emptyList(),
    private val generated: List<Recommendation> = emptyList(),
    private val loadFailure: Exception? = null,
) : RecommendationRepository {
    override suspend fun generate(): List<Recommendation> = generated
    override suspend fun getMyRecommendations(): List<Recommendation> {
        if (loadFailure != null) throw loadFailure
        return myRecs
    }
}
