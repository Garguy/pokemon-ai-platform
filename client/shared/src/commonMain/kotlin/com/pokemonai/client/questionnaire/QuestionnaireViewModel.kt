package com.pokemonai.client.questionnaire

import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.Question
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class QuestionnaireViewModel(
    private val repository: QuestionnaireRepository,
    private val scope: CoroutineScope,
) {
    private val _questions = MutableStateFlow<UiState<List<Question>>>(UiState.Idle)
    val questions: StateFlow<UiState<List<Question>>> = _questions

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState

    fun loadQuestions() {
        _questions.value = UiState.Loading
        scope.launch {
            _questions.value = try {
                UiState.Success(repository.getQuestions())
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to load questions")
            }
        }
    }

    fun submitAnswers(answers: List<AnswerInput>) {
        _submitState.value = UiState.Loading
        scope.launch {
            _submitState.value = try {
                repository.submitAnswers(answers)
                UiState.Success(Unit)
            } catch (e: Exception) {
                UiState.Error(e.message ?: "Failed to submit answers")
            }
        }
    }
}
