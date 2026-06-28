package com.pokemonai.client.questionnaire

import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.Question
import com.pokemonai.client.core.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class QuestionnaireViewModelTest {

    private val threeQuestions = listOf(
        Question("q1", "Do you prefer action?", "ENERGY", 1),
        Question("q2", "Are you curious?", "CURIOSITY", 2),
        Question("q3", "Do you lead?", "LEADERSHIP", 3),
    )

    @Test
    fun `loadQuestions transitions Loading then Success with questions`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeQuestionnaireRepository(questions = threeQuestions)
        val vm = QuestionnaireViewModel(repo, this)

        vm.loadQuestions()

        assertIs<UiState.Success<List<Question>>>(vm.questions.value)
        val loaded = (vm.questions.value as UiState.Success).data
        assertEquals(3, loaded.size)
        assertEquals("q1", loaded[0].id)
    }

    @Test
    fun `loadQuestions transitions Loading then Error on failure`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeQuestionnaireRepository(questionsResult = Result.failure(RuntimeException("Network error")))
        val vm = QuestionnaireViewModel(repo, this)

        vm.loadQuestions()

        assertIs<UiState.Error>(vm.questions.value)
        assertEquals("Network error", (vm.questions.value as UiState.Error).message)
    }

    @Test
    fun `submitAnswers transitions Loading then Success`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeQuestionnaireRepository()
        val vm = QuestionnaireViewModel(repo, this)

        val answers = listOf(
            AnswerInput("q1", 4),
            AnswerInput("q2", 3),
            AnswerInput("q3", 5),
        )
        vm.submitAnswers(answers)

        assertIs<UiState.Success<Unit>>(vm.submitState.value)
        assertEquals(answers, repo.capturedAnswers)
    }

    @Test
    fun `submitAnswers transitions Loading then Error on failure`() = runTest(UnconfinedTestDispatcher()) {
        val repo = FakeQuestionnaireRepository(submitResult = Result.failure(RuntimeException("Submit failed")))
        val vm = QuestionnaireViewModel(repo, this)

        vm.submitAnswers(listOf(AnswerInput("q1", 1)))

        assertIs<UiState.Error>(vm.submitState.value)
        assertEquals("Submit failed", (vm.submitState.value as UiState.Error).message)
    }
}

private class FakeQuestionnaireRepository(
    private val questions: List<Question> = emptyList(),
    private val questionsResult: Result<List<Question>>? = null,
    private val submitResult: Result<Unit> = Result.success(Unit),
) : QuestionnaireRepository {
    var capturedAnswers: List<AnswerInput> = emptyList()

    override suspend fun getQuestions(): List<Question> =
        questionsResult?.getOrThrow() ?: questions

    override suspend fun submitAnswers(answers: List<AnswerInput>) {
        capturedAnswers = answers
        submitResult.getOrThrow()
    }
}
