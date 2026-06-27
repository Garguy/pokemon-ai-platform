package com.pokemonai.pokemon.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonListResponse(int count, List<NamedResource> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedResource(String name, String url) {}
}
