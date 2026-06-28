package com.pokemonai.recommendation.service;

import com.pokemonai.recommendation.domain.*;
import com.pokemonai.shared.exception.PokemonAiException;
import com.pokemonai.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    private static final int TOP_N = 5;

    private final PokemonTraitRepository traitRepository;
    private final RecommendationRepository recommendationRepository;

    public RecommendationEngine(PokemonTraitRepository traitRepository,
                                 RecommendationRepository recommendationRepository) {
        this.traitRepository = traitRepository;
        this.recommendationRepository = recommendationRepository;
    }

    @Transactional
    public List<Recommendation> generate(UUID userId, ProfileVector profile) {
        double[] userVec = profile.toArray();

        List<PokemonTrait> allTraits = traitRepository.findAllOrderedByPokemon();
        Map<UUID, Map<String, BigDecimal>> traitsByPokemon = new LinkedHashMap<>();
        for (PokemonTrait trait : allTraits) {
            traitsByPokemon
                    .computeIfAbsent(trait.getPokemonId(), k -> new HashMap<>())
                    .put(trait.getTraitName(), trait.getTraitScore());
        }

        if (traitsByPokemon.isEmpty()) {
            throw new PokemonAiException("No Pokemon traits found — sync must run before generating recommendations");
        }

        record Scored(UUID pokemonId, BigDecimal score) {}
        List<Scored> scored = traitsByPokemon.entrySet().stream()
                .map(entry -> {
                    double[] pokemonVec = toVector(entry.getValue());
                    BigDecimal sim = CosineSimilarity.compute(userVec, pokemonVec);
                    return new Scored(entry.getKey(), sim);
                })
                .sorted(Comparator.comparing(Scored::score).reversed())
                .limit(TOP_N)
                .toList();

        recommendationRepository.deleteByUserId(userId);
        recommendationRepository.flush();

        List<Recommendation> results = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            Scored s = scored.get(i);
            results.add(recommendationRepository.save(
                    new Recommendation(userId, s.pokemonId(), s.score(), (short) (i + 1))));
        }
        return results;
    }

    public List<Recommendation> findForUser(UUID userId) {
        return recommendationRepository.findByUserIdOrderByRankAsc(userId);
    }

    private double[] toVector(Map<String, BigDecimal> traits) {
        double[] vec = new double[TraitVector.TRAIT_NAMES.length];
        for (int i = 0; i < TraitVector.TRAIT_NAMES.length; i++) {
            BigDecimal score = traits.get(TraitVector.TRAIT_NAMES[i]);
            vec[i] = score != null ? score.doubleValue() : 0.0;
        }
        return vec;
    }

    public record ProfileVector(double energy, double curiosity, double leadership,
                                double loyalty, double risk, double creativity) {
        static {
            // Fail fast if TraitVector.TRAIT_NAMES length changes without updating toArray()
            assert TraitVector.TRAIT_NAMES.length == 6 :
                    "TraitVector.TRAIT_NAMES length changed — update ProfileVector.toArray()";
        }

        public double[] toArray() {
            // Order must match TraitVector.TRAIT_NAMES: ENERGY, CURIOSITY, LEADERSHIP, LOYALTY, RISK, CREATIVITY
            return new double[]{energy, curiosity, leadership, loyalty, risk, creativity};
        }
    }
}
