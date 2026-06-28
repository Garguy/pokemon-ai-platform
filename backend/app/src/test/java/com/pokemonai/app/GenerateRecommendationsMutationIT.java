package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.identity.service.JwtService;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.questionnaire.domain.*;
import com.pokemonai.questionnaire.service.QuestionnaireService;
import com.pokemonai.recommendation.domain.PokemonTrait;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GenerateRecommendationsMutationIT extends PostgresContainerBase {

    @LocalServerPort int port;

    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    @Autowired PokemonRepository pokemonRepository;
    @Autowired PokemonTraitRepository traitRepository;
    @Autowired RecommendationRepository recommendationRepository;
    @Autowired QuestionnaireService questionnaireService;
    @Autowired UserAnswerRepository userAnswerRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired QuestionRepository questionRepository;

    private HttpGraphQlTester tester;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        traitRepository.deleteAll();
        userAnswerRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        pokemonRepository.deleteAll();

        User user = userRepository.save(new User("leaf@pallet.com", passwordEncoder.encode("pass")));
        String token = jwtService.generateToken(user.getEmail());

        tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build());

        // Seed 5 Pokemon with traits
        String[] traitNames = {"ENERGY","CURIOSITY","LEADERSHIP","LOYALTY","RISK","CREATIVITY"};
        for (int i = 1; i <= 5; i++) {
            Pokemon p = pokemonRepository.save(new Pokemon(i, "pokemon-" + i, "desc", "url"));
            for (String trait : traitNames) {
                traitRepository.save(new PokemonTrait(p.getId(), trait, BigDecimal.valueOf(0.1 * i)));
            }
        }

        // Submit answers so a profile exists
        List<QuestionnaireService.AnswerInput> inputs = questionRepository.findAll().stream()
                .map(q -> new QuestionnaireService.AnswerInput(q.getId(), 4))
                .toList();
        questionnaireService.submitAnswers(user.getId(), inputs);
    }

    @Test
    void generateRecommendationsReturnsTopFive() {
        tester.document("""
                mutation {
                    generateRecommendations {
                        rank
                        matchScore
                        pokemon { name }
                    }
                }
                """)
                .execute()
                .path("generateRecommendations").entityList(Map.class).satisfies(recs -> {
                    assertThat(recs).hasSize(5);
                    for (Object r : recs) {
                        Map<?, ?> rec = (Map<?, ?>) r;
                        double score = ((Number) rec.get("matchScore")).doubleValue();
                        assertThat(score).isBetween(0.0, 1.0);
                        assertThat(rec.get("rank")).isNotNull();
                        assertThat(((Map<?, ?>) rec.get("pokemon")).get("name")).isNotNull();
                    }
                });
    }

    @Test
    void myRecommendationsReturnsPersistedResults() {
        // Generate first
        tester.document("mutation { generateRecommendations { rank } }")
                .execute()
                .path("generateRecommendations").entityList(Map.class).hasSize(5);

        // Then query
        tester.document("""
                query {
                    myRecommendations {
                        rank matchScore pokemon { name }
                    }
                }
                """)
                .execute()
                .path("myRecommendations").entityList(Map.class).hasSize(5);
    }
}
