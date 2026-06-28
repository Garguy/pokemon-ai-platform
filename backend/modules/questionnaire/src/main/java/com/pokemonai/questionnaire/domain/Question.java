package com.pokemonai.questionnaire.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(name = "trait_category", nullable = false)
    private TraitCategory traitCategory;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal weight;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected Question() {}

    // Visible for testing
    public Question(TraitCategory traitCategory, BigDecimal weight) {
        this.traitCategory = traitCategory;
        this.weight = weight;
    }

    public UUID getId() { return id; }
    public String getText() { return text; }
    public TraitCategory getTraitCategory() { return traitCategory; }
    public BigDecimal getWeight() { return weight; }
    public int getDisplayOrder() { return displayOrder; }
}
