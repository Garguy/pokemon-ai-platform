package com.pokemonai.client.recommendation

import com.pokemonai.client.core.Recommendation
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecommendationViewModel(
    private val repository: RecommendationRepository,
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow<UiState<List<Recommendation>>>(UiState.Idle)
    val state: StateFlow<UiState<List<Recommendation>>> = _state

    fun load() {
        _state.value = UiState.Loading
        scope.launch {
            _state.value = try {
                val sorted = repository.getMyRecommendations().sortedBy { it.rank }
                UiState.Success(sorted)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load recommendations")
            }
        }
    }

    fun loadHistory() {
        _state.value = UiState.Loading
        scope.launch {
            _state.value = try {
                UiState.Success(repository.getHistory())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load history")
            }
        }
    }

    fun generate() {        _state.value = UiState.Loading
        scope.launch {
            _state.value = try {
                val sorted = repository.generate().sortedBy { it.rank }
                UiState.Success(sorted)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to generate recommendations")
            }
        }
    }
}
