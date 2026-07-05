package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.identity.service.JwtService;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import com.pokemonai.questionnaire.domain.QuestionRepository;
import com.pokemonai.questionnaire.domain.UserAnswerRepository;
import com.pokemonai.questionnaire.service.QuestionnaireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MyProfileQueryIT extends PostgresContainerBase {

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired UserAnswerRepository userAnswerRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired QuestionRepository questionRepository;
    @Autowired QuestionnaireService questionnaireService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User user;
    private HttpGraphQlTester tester;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(new User("serena@kalos.com", passwordEncoder.encode("fennekin")));
        String token = jwtService.generateToken(user.getEmail());

        tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build());
    }

    @Test
    void myProfileReturnsNullBeforeSubmittingAnswers() {
        tester.document("""
                query {
                    myProfile {
                        energy curiosity
                    }
                }
                """)
                .execute()
                .path("myProfile").valueIsNull();
    }

    @Test
    void myProfileReturnsCalculatedScoresAfterSubmission() {
        var inputs = questionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(q -> new QuestionnaireService.AnswerInput(q.getId(), 5))
                .toList();
        questionnaireService.submitAnswers(user.getId(), inputs);

        tester.document("""
                query {
                    myProfile {
                        energy curiosity leadership loyalty risk creativity calculatedAt
                    }
                }
                """)
                .execute()
                .path("myProfile").entity(Map.class).satisfies(profile -> {
                    // all answers=5 → score=1.0 for every trait
                    for (String trait : new String[]{"energy", "curiosity", "leadership", "loyalty", "risk", "creativity"}) {
                        double score = ((Number) profile.get(trait)).doubleValue();
                        assertThat(score).as(trait).isEqualTo(1.0, org.assertj.core.api.Assertions.within(0.001));
                    }
                    assertThat(profile.get("calculatedAt")).isNotNull();
                });
    }
}
