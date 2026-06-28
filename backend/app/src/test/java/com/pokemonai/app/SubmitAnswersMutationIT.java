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
}
