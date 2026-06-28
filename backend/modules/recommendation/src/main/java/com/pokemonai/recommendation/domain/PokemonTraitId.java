package com.pokemonai.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PokemonTraitId implements Serializable {

    @Column(name = "pokemon_id")
    private UUID pokemonId;

    @Column(name = "trait_name")
    private String traitName;

    protected PokemonTraitId() {}

    public PokemonTraitId(UUID pokemonId, String traitName) {
        this.pokemonId = pokemonId;
        this.traitName = traitName;
    }

    public UUID getPokemonId() { return pokemonId; }
    public String getTraitName() { return traitName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PokemonTraitId other)) return false;
        return Objects.equals(pokemonId, other.pokemonId) && Objects.equals(traitName, other.traitName);
    }

    @Override
    public int hashCode() { return Objects.hash(pokemonId, traitName); }
}
