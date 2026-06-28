package com.pokemonai.client.questionnaire

import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.Question

interface QuestionnaireRepository {
    suspend fun getQuestions(): List<Question>
    suspend fun submitAnswers(answers: List<AnswerInput>)
}
