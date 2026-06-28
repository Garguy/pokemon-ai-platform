package com.pokemonai.recommendation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    List<Recommendation> findByUserIdOrderByRankAsc(UUID userId);

    void deleteByUserId(UUID userId);
}
