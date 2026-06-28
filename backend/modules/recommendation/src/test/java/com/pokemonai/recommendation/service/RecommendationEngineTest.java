package com.pokemonai.recommendation.service;

import com.pokemonai.recommendation.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationEngineTest {

    @Mock PokemonTraitRepository traitRepository;
    @Mock RecommendationRepository recommendationRepository;

    RecommendationEngine engine;

    // Three Pokemon with distinct trait vectors
    UUID pikachu  = UUID.randomUUID();
    UUID snorlax  = UUID.randomUUID();
    UUID mewtwo   = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine(traitRepository, recommendationRepository);

        // User profile: high energy, high risk, low loyalty
        // pikachu  = [0.9, 0.8, 0.5, 0.3, 0.9, 0.7] → closest match
        // snorlax  = [0.2, 0.2, 0.1, 0.9, 0.1, 0.2] → worst match (high loyalty, low energy)
        // mewtwo   = [0.5, 0.9, 0.9, 0.2, 0.5, 0.9] → mid match
        List<PokemonTrait> traits = buildTraits(pikachu,  0.9, 0.8, 0.5, 0.3, 0.9, 0.7);
        traits.addAll(buildTraits(snorlax,              0.2, 0.2, 0.1, 0.9, 0.1, 0.2));
        traits.addAll(buildTraits(mewtwo,               0.5, 0.9, 0.9, 0.2, 0.5, 0.9));
        when(traitRepository.findAllOrderedByPokemon()).thenReturn(traits);
        when(recommendationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void bestMatchIsFirstInResults() {
        // User: high energy, high risk matches pikachu best
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.7, 0.4, 0.2, 0.9, 0.6);
        List<Recommendation> results = engine.generate(UUID.randomUUID(), profile);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getPokemonId()).isEqualTo(pikachu);
        assertThat(results.get(2).getPokemonId()).isEqualTo(snorlax);
    }

    @Test
    void ranksAreAssignedOneThrough() {
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.7, 0.4, 0.2, 0.9, 0.6);
        List<Recommendation> results = engine.generate(UUID.randomUUID(), profile);

        for (int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getRank()).isEqualTo((short) (i + 1));
        }
    }

    @Test
    void scoresAreDescending() {
        var profile = new RecommendationEngine.ProfileVector(0.9, 0.7, 0.4, 0.2, 0.9, 0.6);
        List<Recommendation> results = engine.generate(UUID.randomUUID(), profile);

        for (int i = 0; i < results.size() - 1; i++) {
            assertThat(results.get(i).getMatchScore())
                    .isGreaterThanOrEqualTo(results.get(i + 1).getMatchScore());
        }
    }

    // --- helpers ---

    private List<PokemonTrait> buildTraits(UUID pokemonId,
                                            double energy, double curiosity, double leadership,
                                            double loyalty, double risk, double creativity) {
        double[] vals = {energy, curiosity, leadership, loyalty, risk, creativity};
        List<PokemonTrait> list = new ArrayList<>();
        for (int i = 0; i < TraitVector.TRAIT_NAMES.length; i++) {
            list.add(new PokemonTrait(pokemonId, TraitVector.TRAIT_NAMES[i],
                    BigDecimal.valueOf(vals[i])));
        }
        return list;
    }
}
