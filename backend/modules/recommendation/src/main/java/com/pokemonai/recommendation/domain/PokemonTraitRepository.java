package com.pokemonai.recommendation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PokemonTraitRepository extends JpaRepository<PokemonTrait, PokemonTraitId> {

    List<PokemonTrait> findByIdPokemonId(UUID pokemonId);

    @Query("SELECT t FROM PokemonTrait t ORDER BY t.id.pokemonId, t.id.traitName")
    List<PokemonTrait> findAllOrderedByPokemon();
}
