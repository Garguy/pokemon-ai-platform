package com.pokemonai.app;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.questionnaire.domain.PersonalityProfile;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import com.pokemonai.recommendation.domain.PokemonTrait;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import com.pokemonai.recommendation.service.RecommendationEngine;
import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationEngineIT extends PostgresContainerBase {

    @Autowired RecommendationEngine recommendationEngine;
    @Autowired PokemonRepository pokemonRepository;
    @Autowired PokemonTraitRepository traitRepository;
    @Autowired RecommendationRepository recommendationRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private UUID userId;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        traitRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
        pokemonRepository.deleteAll();

        User user = userRepository.save(new User("red@pallet.com", passwordEncoder.encode("charmander")));
        userId = user.getId();

        // 5 Pokemon with distinct trait vectors
        seedPokemon(1, "bulbasaur",    0.3, 0.7, 0.4, 0.8, 0.2, 0.9); // high curiosity+creativity
        seedPokemon(2, "charmander",   0.9, 0.5, 0.8, 0.3, 0.8, 0.5); // high energy+leadership+risk
        seedPokemon(3, "squirtle",     0.5, 0.5, 0.5, 0.5, 0.5, 0.5); // neutral
        seedPokemon(4, "caterpie",     0.1, 0.2, 0.1, 0.9, 0.1, 0.2); // high loyalty
        seedPokemon(5, "weedle",       0.7, 0.3, 0.6, 0.2, 0.7, 0.3); // high energy+risk

        // User profile matching charmander (high energy, leadership, risk)
        profileRepository.save(new PersonalityProfile(userId,
                bd("0.9"), bd("0.5"), bd("0.8"), bd("0.2"), bd("0.9"), bd("0.4")));
    }

    @Test
    void generatePersistsFiveRecommendations() {
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.5, 0.8, 0.2, 0.9, 0.4);

        List<Recommendation> results = recommendationEngine.generate(userId, profile);

        assertThat(results).hasSize(5);
        assertThat(recommendationRepository.findByUserIdOrderByRankAsc(userId)).hasSize(5);
    }

    @Test
    void bestMatchIsCharmanderForHighEnergyRiskProfile() {
        // Profile mirrors charmander [0.9, 0.5, 0.8, 0.3, 0.8, 0.5] closely
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.5, 0.8, 0.3, 0.8, 0.5);

        List<Recommendation> results = recommendationEngine.generate(userId, profile);

        UUID top = results.get(0).getPokemonId();
        Pokemon topPokemon = pokemonRepository.findById(top).orElseThrow();
        assertThat(topPokemon.getName()).isEqualTo("charmander");
    }

    @Test
    void regenerationReplacesExistingRecommendations() {
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.5, 0.8, 0.2, 0.9, 0.4);
        recommendationEngine.generate(userId, profile);
        recommendationEngine.generate(userId, profile);

        assertThat(recommendationRepository.findByUserIdOrderByRankAsc(userId)).hasSize(5);
    }

    @Test
    void ranksAreSequential() {
        var profile = new RecommendationEngine.ProfileVector(0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        List<Recommendation> results = recommendationEngine.generate(userId, profile);

        for (int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getRank()).isEqualTo((short)(i + 1));
        }
    }

    private void seedPokemon(int extId, String name,
                              double energy, double curiosity, double leadership,
                              double loyalty, double risk, double creativity) {
        Pokemon p = pokemonRepository.save(new Pokemon(extId, name, "desc", "url"));
        double[] vals = {energy, curiosity, leadership, loyalty, risk, creativity};
        String[] names = {"ENERGY","CURIOSITY","LEADERSHIP","LOYALTY","RISK","CREATIVITY"};
        for (int i = 0; i < names.length; i++) {
            traitRepository.save(new PokemonTrait(p.getId(), names[i], BigDecimal.valueOf(vals[i])));
        }
    }

    private BigDecimal bd(String val) { return new BigDecimal(val); }
}
