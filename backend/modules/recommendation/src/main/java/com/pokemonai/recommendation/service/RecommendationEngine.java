package com.pokemonai.recommendation.service;

import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RecommendationEngine {

    private final RecommendationRepository recommendationRepository;

    public RecommendationEngine(RecommendationRepository recommendationRepository) {
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public List<Recommendation> save(UUID userId, List<RankedPokemon> ranked) {
        recommendationRepository.deleteByUserId(userId);
        recommendationRepository.flush();

        List<Recommendation> results = new ArrayList<>();
        for (int i = 0; i < ranked.size(); i++) {
            RankedPokemon rp = ranked.get(i);
            Recommendation rec = new Recommendation(userId, rp.pokemonId(), rp.score(), (short) (i + 1));
            rec.setExplanation(rp.explanation());
            results.add(recommendationRepository.save(rec));
        }
        return results;
    }

    public List<Recommendation> findForUser(UUID userId) {
        return recommendationRepository.findByUserIdOrderByRankAsc(userId);
    }

    public record RankedPokemon(UUID pokemonId, BigDecimal score, String explanation) {}
}
