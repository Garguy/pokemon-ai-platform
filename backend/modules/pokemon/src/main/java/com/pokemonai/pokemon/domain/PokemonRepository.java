package com.pokemonai.pokemon.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PokemonRepository extends JpaRepository<Pokemon, UUID> {

    Optional<Pokemon> findByExternalId(int externalId);

    Optional<Pokemon> findByNameIgnoreCase(String name);

    boolean existsByTypesCsvIsNull();

    Page<Pokemon> findAllByOrderByExternalIdAsc(Pageable pageable);

    @Query("select p.name from Pokemon p order by p.externalId asc")
    List<String> findAllNames();
}
