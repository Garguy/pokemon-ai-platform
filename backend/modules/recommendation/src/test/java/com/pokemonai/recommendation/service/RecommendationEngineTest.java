package com.pokemonai.recommendation.service;

import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecommendationEngineTest {

    @Mock RecommendationRepository recommendationRepository;

    RecommendationEngine engine;

    UUID userId  = UUID.randomUUID();
    UUID pikachu = UUID.randomUUID();
    UUID snorlax = UUID.randomUUID();
    UUID mewtwo  = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine(recommendationRepository);
        when(recommendationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(recommendationRepository.findBatchIdsNewestFirst(any())).thenReturn(List.of());
        when(recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void savePersistedAllRankedPokemon() {
        List<RecommendationEngine.RankedPokemon> ranked = List.of(
                new RecommendationEngine.RankedPokemon(pikachu, BigDecimal.valueOf(0.95), "Best match"),
                new RecommendationEngine.RankedPokemon(mewtwo,  BigDecimal.valueOf(0.75), "Good match"),
                new RecommendationEngine.RankedPokemon(snorlax, BigDecimal.valueOf(0.55), "Partial match")
        );

        List<Recommendation> results = engine.save(userId, ranked);

        assertThat(results).hasSize(3);
        verify(recommendationRepository, times(3)).save(any());
    }

    @Test
    void ranksAreAssignedOneThrough() {
        List<RecommendationEngine.RankedPokemon> ranked = List.of(
                new RecommendationEngine.RankedPokemon(pikachu, BigDecimal.valueOf(0.9), "e1"),
                new RecommendationEngine.RankedPokemon(mewtwo,  BigDecimal.valueOf(0.7), "e2"),
                new RecommendationEngine.RankedPokemon(snorlax, BigDecimal.valueOf(0.5), "e3")
        );

        List<Recommendation> results = engine.save(userId, ranked);

        for (int i = 0; i < results.size(); i++) {
            assertThat(results.get(i).getRank()).isEqualTo((short) (i + 1));
        }
    }

    @Test
    void savePreservesScoresAndExplanations() {
        List<Recommendation> results = engine.save(userId, List.of(
                new RecommendationEngine.RankedPokemon(pikachu, BigDecimal.valueOf(0.97), "High energy match")
        ));

        assertThat(results.get(0).getMatchScore()).isEqualByComparingTo("0.97");
        assertThat(results.get(0).getExplanation()).isEqualTo("High energy match");
        assertThat(results.get(0).getPokemonId()).isEqualTo(pikachu);
        assertThat(results.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void saveAssignsSharedBatchIdAcrossResults() {
        List<Recommendation> results = engine.save(userId, List.of(
                new RecommendationEngine.RankedPokemon(pikachu, BigDecimal.valueOf(0.9), "e1"),
                new RecommendationEngine.RankedPokemon(mewtwo,  BigDecimal.valueOf(0.7), "e2")
        ));

        UUID batchId = results.get(0).getBatchId();
        assertThat(batchId).isNotNull();
        assertThat(results.get(1).getBatchId()).isEqualTo(batchId);
    }

    @Test
    void findForUserReturnsEmptyListWhenNoRecommendationsExist() {
        List<Recommendation> results = engine.findForUser(userId);

        assertThat(results).isEmpty();
    }

    @Test
    void findForUserReturnsBatchMatchingLatestRecommendation() {
        UUID batchId = UUID.randomUUID();
        Recommendation r1 = new Recommendation(userId, pikachu, batchId,
                BigDecimal.valueOf(0.9), (short) 1, OffsetDateTime.now());
        Recommendation r2 = new Recommendation(userId, snorlax, batchId,
                BigDecimal.valueOf(0.7), (short) 2, OffsetDateTime.now());

        when(recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId))
                .thenReturn(Optional.of(r1));
        when(recommendationRepository.findByUserIdAndBatchIdOrderByRankAsc(userId, batchId))
                .thenReturn(List.of(r1, r2));

        List<Recommendation> results = engine.findForUser(userId);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getRank()).isEqualTo((short) 1);
    }

    @Test
    void pruningTriggeredWhenBatchCountExceedsTen() {
        List<UUID> elevenBatches = new ArrayList<>();
        for (int i = 0; i < 11; i++) elevenBatches.add(UUID.randomUUID());
        when(recommendationRepository.findBatchIdsNewestFirst(userId)).thenReturn(elevenBatches);

        engine.save(userId, List.of(
                new RecommendationEngine.RankedPokemon(pikachu, BigDecimal.valueOf(0.9), "e")
        ));

        verify(recommendationRepository).deleteByUserIdAndBatchIdIn(
                userId, elevenBatches.subList(10, 11));
    }
}
