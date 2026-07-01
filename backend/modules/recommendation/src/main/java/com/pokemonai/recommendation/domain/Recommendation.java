package com.pokemonai.recommendation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "pokemon_id", nullable = false)
    private UUID pokemonId;

    @Column(name = "batch_id", nullable = false)
    private UUID batchId;

    @Column(name = "match_score", nullable = false, precision = 7, scale = 6)
    private BigDecimal matchScore;

    @Column(name = "rank", nullable = false)
    private short rank;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt = OffsetDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String explanation;

    protected Recommendation() {}

    public Recommendation(UUID userId, UUID pokemonId, BigDecimal matchScore, short rank) {
        this.userId = userId;
        this.pokemonId = pokemonId;
        this.matchScore = matchScore;
        this.rank = rank;
    }

    public Recommendation(UUID userId, UUID pokemonId, UUID batchId, BigDecimal matchScore,
                          short rank, OffsetDateTime generatedAt) {
        this.userId = userId;
        this.pokemonId = pokemonId;
        this.batchId = batchId;
        this.matchScore = matchScore;
        this.rank = rank;
        this.generatedAt = generatedAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getPokemonId() { return pokemonId; }
    public UUID getBatchId() { return batchId; }
    public BigDecimal getMatchScore() { return matchScore; }
    public short getRank() { return rank; }
    public OffsetDateTime getGeneratedAt() { return generatedAt; }
    public String getExplanation() { return explanation; }

    public void setExplanation(String explanation) { this.explanation = explanation; }
}
