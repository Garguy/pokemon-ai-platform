package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.identity.service.JwtService;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import com.pokemonai.questionnaire.domain.UserAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that protected mutations reject requests with no token, and that
 * the unauthenticated myProfile query returns null rather than an error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UnauthenticatedRequestIT extends PostgresContainerBase {

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired UserAnswerRepository userAnswerRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private HttpGraphQlTester anonTester;
    private HttpGraphQlTester authTester;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        anonTester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());

        User user = userRepository.save(new User("dawn@sinnoh.com", passwordEncoder.encode("piplup")));
        String token = jwtService.generateToken(user.getEmail());
        authTester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build());
    }

    @Test
    void submitAnswersWithoutTokenReturnsError() {
        anonTester.document("""
                mutation {
                    submitAnswers(answers: [{ questionId: "00000000-0000-0000-0000-000000000001", answerValue: 3 }]) {
                        energy
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void generateRecommendationsWithoutTokenReturnsError() {
        anonTester.document("""
                mutation {
                    generateRecommendations {
                        rank matchScore
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void myRecommendationsWithoutTokenReturnsError() {
        anonTester.document("""
                query {
                    myRecommendations {
                        rank matchScore
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }

    @Test
    void myProfileWithoutTokenReturnsNull() {
        // myProfile is nullable — unauthenticated returns null, not an error
        anonTester.document("""
                query {
                    myProfile {
                        energy
                    }
                }
                """)
                .execute()
                .path("myProfile").valueIsNull();
    }

    @Test
    void myProfileAuthenticatedWithNoSubmissionReturnsNull() {
        // User exists but has never submitted answers — profile is absent
        authTester.document("""
                query {
                    myProfile {
                        energy
                    }
                }
                """)
                .execute()
                .path("myProfile").valueIsNull();
    }
}
