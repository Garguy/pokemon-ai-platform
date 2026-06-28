package com.pokemonai.client.questionnaire

import com.apollographql.apollo.ApolloClient
import com.pokemonai.client.core.AnswerInput
import com.pokemonai.client.core.PersonalityProfile
import com.pokemonai.client.core.Question
import com.pokemonai.client.graphql.GetQuestionsQuery
import com.pokemonai.client.graphql.MyProfileQuery
import com.pokemonai.client.graphql.SubmitAnswersMutation
import com.pokemonai.client.graphql.type.AnswerInput as GqlAnswerInput

class ApolloQuestionnaireRepository(private val apolloClient: ApolloClient) : QuestionnaireRepository {

    override suspend fun getQuestions(): List<Question> {
        val response = apolloClient.query(GetQuestionsQuery()).execute()
        return response.dataOrThrow().questions.map {
            Question(it.id, it.text, it.traitCategory, it.displayOrder)
        }
    }

    override suspend fun submitAnswers(answers: List<AnswerInput>) {
        val gqlAnswers = answers.map { GqlAnswerInput(it.questionId, it.answerValue) }
        apolloClient.mutation(SubmitAnswersMutation(gqlAnswers)).execute().dataOrThrow()
    }

    override suspend fun getProfile(): PersonalityProfile? {
        val p = apolloClient.query(MyProfileQuery()).execute().dataOrThrow().myProfile
            ?: return null
        return PersonalityProfile(p.energy, p.curiosity, p.leadership, p.loyalty, p.risk, p.creativity)
    }
}
