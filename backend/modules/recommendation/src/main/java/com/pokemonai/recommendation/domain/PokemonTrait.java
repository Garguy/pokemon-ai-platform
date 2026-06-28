package com.pokemonai.recommendation.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "pokemon_traits")
public class PokemonTrait {

    @EmbeddedId
    private PokemonTraitId id;

    @Column(name = "trait_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal traitScore;

    protected PokemonTrait() {}

    public PokemonTrait(UUID pokemonId, String traitName, BigDecimal traitScore) {
        this.id = new PokemonTraitId(pokemonId, traitName);
        this.traitScore = traitScore;
    }

    public UUID getPokemonId() { return id.getPokemonId(); }
    public String getTraitName() { return id.getTraitName(); }
    public BigDecimal getTraitScore() { return traitScore; }
}
