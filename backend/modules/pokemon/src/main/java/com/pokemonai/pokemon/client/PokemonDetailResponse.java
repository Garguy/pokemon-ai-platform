package com.pokemonai.pokemon.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonDetailResponse(
        int id,
        String name,
        Sprites sprites
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sprites(
            @JsonProperty("front_default") String frontDefault,
            Other other
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Other(
            @JsonProperty("official-artwork") OfficialArtwork officialArtwork
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OfficialArtwork(
            @JsonProperty("front_default") String frontDefault
    ) {}

    public String bestImageUrl() {
        if (sprites != null
                && sprites.other() != null
                && sprites.other().officialArtwork() != null
                && sprites.other().officialArtwork().frontDefault() != null) {
            return sprites.other().officialArtwork().frontDefault();
        }
        if (sprites != null) {
            return sprites.frontDefault();
        }
        return null;
    }
}
