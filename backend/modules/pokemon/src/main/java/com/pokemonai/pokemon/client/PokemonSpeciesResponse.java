package com.pokemonai.pokemon.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonSpeciesResponse(
        int id,
        String name,
        @JsonProperty("flavor_text_entries") List<FlavorTextEntry> flavorTextEntries
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlavorTextEntry(
            @JsonProperty("flavor_text") String flavorText,
            Language language
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Language(String name) {}

    public String englishFlavorText() {
        if (flavorTextEntries == null) return null;
        return flavorTextEntries.stream()
                .filter(e -> "en".equals(e.language().name()))
                .map(FlavorTextEntry::flavorText)
                // PokeAPI flavor text contains form feeds and newlines
                .map(t -> t.replaceAll("[\\f\\n\\r]", " ").trim())
                .findFirst()
                .orElse(null);
    }
}
