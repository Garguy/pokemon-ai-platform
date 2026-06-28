package com.pokemonai.questionnaire.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "personality_profiles")
public class PersonalityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal energy;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal curiosity;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal leadership;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal loyalty;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal risk;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal creativity;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt = OffsetDateTime.now();

    protected PersonalityProfile() {}

    public PersonalityProfile(UUID userId, BigDecimal energy, BigDecimal curiosity,
                               BigDecimal leadership, BigDecimal loyalty,
                               BigDecimal risk, BigDecimal creativity) {
        this.userId = userId;
        this.energy = energy;
        this.curiosity = curiosity;
        this.leadership = leadership;
        this.loyalty = loyalty;
        this.risk = risk;
        this.creativity = creativity;
    }

    public void update(BigDecimal energy, BigDecimal curiosity, BigDecimal leadership,
                       BigDecimal loyalty, BigDecimal risk, BigDecimal creativity) {
        this.energy = energy;
        this.curiosity = curiosity;
        this.leadership = leadership;
        this.loyalty = loyalty;
        this.risk = risk;
        this.creativity = creativity;
        this.calculatedAt = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public BigDecimal getEnergy() { return energy; }
    public BigDecimal getCuriosity() { return curiosity; }
    public BigDecimal getLeadership() { return leadership; }
    public BigDecimal getLoyalty() { return loyalty; }
    public BigDecimal getRisk() { return risk; }
    public BigDecimal getCreativity() { return creativity; }
    public OffsetDateTime getCalculatedAt() { return calculatedAt; }
}
