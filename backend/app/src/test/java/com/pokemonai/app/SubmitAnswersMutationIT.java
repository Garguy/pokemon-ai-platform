package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.identity.service.JwtService;
import com.pokemonai.questionnaire.domain.QuestionRepository;
import com.pokemonai.questionnaire.domain.UserAnswerRepository;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmitAnswersMutationIT extends PostgresContainerBase {

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired UserAnswerRepository userAnswerRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private HttpGraphQlTester tester;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User("ash@kanto.com", passwordEncoder.encode("pikachu")));
        String token = jwtService.generateToken(user.getEmail());

        tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build());
    }

    @Test
    void submitAllAnswersReturnsProfileWithScoresInRange() {
        var questions = questionRepository.findAllByOrderByDisplayOrderAsc();

        String answersArg = questions.stream()
                .map(q -> """
                        { questionId: "%s", answerValue: 4 }
                        """.formatted(q.getId()))
                .collect(Collectors.joining(", "));

        tester.document("""
                mutation {
                    submitAnswers(answers: [%s]) {
                        energy curiosity leadership loyalty risk creativity
                    }
                }
                """.formatted(answersArg))
                .execute()
                .path("submitAnswers").entity(Map.class).satisfies(profile -> {
                    List<String> traits = List.of("energy", "curiosity", "leadership", "loyalty", "risk", "creativity");
                    for (String trait : traits) {
                        double score = ((Number) profile.get(trait)).doubleValue();
                        assertThat(score).as(trait).isBetween(0.0, 1.0);
                        // all answers=4 → 4/5 = 0.8
                        assertThat(score).as(trait).isEqualTo(0.8, org.assertj.core.api.Assertions.within(0.0001));
                    }
                });
    }

    @Test
    void answerValueBelowRangeReturnsError() {
        var question = questionRepository.findAllByOrderByDisplayOrderAsc().get(0);

        tester.document("""
                mutation {
                    submitAnswers(answers: [{ questionId: "%s", answerValue: 0 }]) {
                        energy
                    }
                }
                """.formatted(question.getId()))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void answerValueAboveRangeReturnsError() {
        var question = questionRepository.findAllByOrderByDisplayOrderAsc().get(0);

        tester.document("""
                mutation {
                    submitAnswers(answers: [{ questionId: "%s", answerValue: 6 }]) {
                        energy
                    }
                }
                """.formatted(question.getId()))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void duplicateQuestionIdInSameSubmissionReturnsError() {
        var question = questionRepository.findAllByOrderByDisplayOrderAsc().get(0);
        String qid = question.getId().toString();

        tester.document("""
                mutation {
                    submitAnswers(answers: [
                        { questionId: "%s", answerValue: 3 },
                        { questionId: "%s", answerValue: 4 }
                    ]) {
                        energy
                    }
                }
                """.formatted(qid, qid))
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void malformedQuestionIdFormatReturnsError() {
        tester.document("""
                mutation {
                    submitAnswers(answers: [{ questionId: "not-a-uuid", answerValue: 3 }]) {
                        energy
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void unknownQuestionIdReturnsError() {
        tester.document("""
                mutation {
                    submitAnswers(answers: [{
                        questionId: "00000000-0000-0000-0000-000000000000",
                        answerValue: 3
                    }]) {
                        energy
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
