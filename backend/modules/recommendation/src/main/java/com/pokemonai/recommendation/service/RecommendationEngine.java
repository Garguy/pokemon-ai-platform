package com.pokemonai.recommendation.service;

import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationEngine {

    private static final int MAX_TESTS = 10;

    private final RecommendationRepository recommendationRepository;

    public RecommendationEngine(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public List<Recommendation> save(UUID userId, List<RankedPokemon> ranked) {
        UUID batchId = UUID.randomUUID();
        OffsetDateTime generatedAt = OffsetDateTime.now();

        List<Recommendation> results = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            RankedPokemon rp = ranked.get(i);
            Recommendation rec = new Recommendation(
                    userId, rp.pokemonId(), batchId, rp.score(), (short) (i + 1), generatedAt);
            rec.setExplanation(rp.explanation());
            results.add(recommendationRepository.save(rec));
        }

        pruneOldTests(userId);
        return results;
    }

    private void pruneOldTests(UUID userId) {
        List<UUID> batches = recommendationRepository.findBatchIdsNewestFirst(userId);
        if (batches.size() > MAX_TESTS) {
            recommendationRepository.deleteByUserIdAndBatchIdIn(
                    userId, batches.subList(MAX_TESTS, batches.size()));
        }
    }

    public List<Recommendation> findForUser(UUID userId) {
        return recommendationRepository.findFirstByUserIdOrderByGeneratedAtDesc(userId)
                .map(latest -> recommendationRepository
                        .findByUserIdAndBatchIdOrderByRankAsc(userId, latest.getBatchId()))
                .orElseGet(List::of);
    }

    public List<Recommendation> findHistory(UUID userId) {
        // Only the most recent tests remain in storage (pruned to MAX_TESTS),
        // so this returns every match from each recent test, newest test first.
        return recommendationRepository.findByUserIdOrderByGeneratedAtDescRankAsc(userId);
    }

    public record RankedPokemon(UUID pokemonId, BigDecimal score, String explanation) {}
}
