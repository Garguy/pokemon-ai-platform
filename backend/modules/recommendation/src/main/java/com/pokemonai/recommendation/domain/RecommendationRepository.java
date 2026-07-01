package com.pokemonai.recommendation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    Optional<Recommendation> findFirstByUserIdOrderByGeneratedAtDesc(UUID userId);

    List<Recommendation> findByUserIdAndBatchIdOrderByRankAsc(UUID userId, UUID batchId);

    List<Recommendation> findByUserIdOrderByGeneratedAtDescRankAsc(UUID userId);

    @Query("select r.batchId from Recommendation r where r.userId = :userId " +
           "group by r.batchId order by max(r.generatedAt) desc")
    List<UUID> findBatchIdsNewestFirst(@Param("userId") UUID userId);

    void deleteByUserIdAndBatchIdIn(UUID userId, List<UUID> batchIds);
}
