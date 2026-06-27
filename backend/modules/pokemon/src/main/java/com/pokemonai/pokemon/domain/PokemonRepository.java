package com.pokemonai.pokemon.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PokemonRepository extends JpaRepository<Pokemon, UUID> {

    Optional<Pokemon> findByExternalId(int externalId);

    Page<Pokemon> findAllByOrderByExternalIdAsc(Pageable pageable);
}
