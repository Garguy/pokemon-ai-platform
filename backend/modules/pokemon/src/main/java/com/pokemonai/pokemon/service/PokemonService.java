package com.pokemonai.pokemon.service;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.shared.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PokemonService {

    private final PokemonRepository pokemonRepository;

    public PokemonService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    @Cacheable("pokemon-by-id")
    public Pokemon findById(UUID id) {
        return pokemonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pokemon", id));
    }

    @Cacheable("pokemon-page")
    public Page<Pokemon> findAll(int page, int size) {
        return pokemonRepository.findAllByOrderByExternalIdAsc(PageRequest.of(page, size));
    }
}
