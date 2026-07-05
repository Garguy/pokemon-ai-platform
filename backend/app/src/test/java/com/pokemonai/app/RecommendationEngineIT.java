package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.recommendation.domain.PokemonTrait;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import com.pokemonai.recommendation.service.RecommendationEngine;
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
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private UUID userId;
    private UUID pikachuId;
    private UUID snorlaxId;

    @BeforeEach
    void setUp() {
        recommendationRepository.deleteAll();
        traitRepository.deleteAll();
        userRepository.deleteAll();
        pokemonRepository.deleteAll();

        User user = userRepository.save(new User("red@pallet.com", passwordEncoder.encode("charmander")));
        userId = user.getId();

        pikachuId = seedPokemon(1, "pikachu",   0.9, 0.7, 0.5, 0.3, 0.9, 0.7);
        seedPokemon(2, "charmander",             0.8, 0.5, 0.8, 0.3, 0.7, 0.5);
        seedPokemon(3, "squirtle",               0.5, 0.5, 0.5, 0.5, 0.5, 0.5);
        seedPokemon(4, "caterpie",               0.1, 0.2, 0.1, 0.9, 0.1, 0.2);
        snorlaxId = seedPokemon(5, "snorlax",   0.2, 0.2, 0.1, 0.9, 0.1, 0.3);
    }

    @Test
    void savePersistsAllRankedEntries() {
        List<RecommendationEngine.RankedPokemon> ranked = List.of(
                new RecommendationEngine.RankedPokemon(pikachuId, BigDecimal.valueOf(0.95), "High energy match"),
                new RecommendationEngine.RankedPokemon(snorlaxId, BigDecimal.valueOf(0.60), "Partial match")
        );

        List<Recommendation> results = recommendationEngine.save(userId, ranked);

        assertThat(results).hasSize(2);
        List<Recommendation> inDb = recommendationEngine.findForUser(userId);
        assertThat(inDb).hasSize(2);
    }

    @Test
    void ranksAreSequentialStartingAtOne() {
        List<RecommendationEngine.RankedPokemon> ranked = List.of(
                new RecommendationEngine.RankedPokemon(pikachuId, BigDecimal.valueOf(0.9), "e1"),
                new RecommendationEngine.RankedPokemon(snorlaxId, BigDecimal.valueOf(0.6), "e2")
        );

        List<Recommendation> results = recommendationEngine.save(userId, ranked);

        assertThat(results.get(0).getRank()).isEqualTo((short) 1);
        assertThat(results.get(1).getRank()).isEqualTo((short) 2);
    }

    @Test
    void secondSavePushesFirstBatchIntoHistory() {
        List<RecommendationEngine.RankedPokemon> batch = List.of(
                new RecommendationEngine.RankedPokemon(pikachuId, BigDecimal.valueOf(0.9), "e1")
        );

        recommendationEngine.save(userId, batch);
        recommendationEngine.save(userId, batch);

        // findForUser returns latest batch only
        assertThat(recommendationEngine.findForUser(userId)).hasSize(1);
        // findHistory returns both batches
        assertThat(recommendationEngine.findHistory(userId)).hasSize(2);
    }

    @Test
    void findForUserReturnsEmptyListWhenNoneExist() {
        assertThat(recommendationEngine.findForUser(userId)).isEmpty();
    }

    @Test
    void scoresAndExplanationsArePersisted() {
        List<RecommendationEngine.RankedPokemon> ranked = List.of(
                new RecommendationEngine.RankedPokemon(pikachuId, BigDecimal.valueOf(0.97), "Best match for high energy")
        );

        List<Recommendation> results = recommendationEngine.save(userId, ranked);

        assertThat(results.get(0).getMatchScore()).isEqualByComparingTo("0.97");
        assertThat(results.get(0).getExplanation()).isEqualTo("Best match for high energy");
    }

    private UUID seedPokemon(int extId, String name,
                              double energy, double curiosity, double leadership,
                              double loyalty, double risk, double creativity) {
        Pokemon p = pokemonRepository.save(new Pokemon(extId, name, "desc", "url"));
        double[] vals = {energy, curiosity, leadership, loyalty, risk, creativity};
        String[] names = {"ENERGY", "CURIOSITY", "LEADERSHIP", "LOYALTY", "RISK", "CREATIVITY"};
        for (int i = 0; i < names.length; i++) {
            traitRepository.save(new PokemonTrait(p.getId(), names[i], BigDecimal.valueOf(vals[i])));
        }
        return p.getId();
    }
}
