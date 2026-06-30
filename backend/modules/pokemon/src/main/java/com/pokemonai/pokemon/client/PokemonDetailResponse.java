package com.pokemonai.pokemon.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Comparator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonDetailResponse(
        int id,
        String name,
        int height,
        int weight,
        List<TypeSlot> types,
        List<StatSlot> stats,
        Sprites sprites
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TypeSlot(int slot, Type type) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Type(String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatSlot(@JsonProperty("base_stat") int baseStat, Stat stat) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Stat(String name) {}

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

    /** Type slugs ordered by slot, e.g. ["grass", "poison"]. */
    public List<String> typeNames() {
        if (types == null) return List.of();
        return types.stream()
                .sorted(Comparator.comparingInt(TypeSlot::slot))
                .map(t -> t.type().name())
                .toList();
    }

    /** Base stat by PokeAPI name (e.g. "hp", "special-attack"), or null if absent. */
    public Integer baseStat(String statName) {
        if (stats == null) return null;
        return stats.stream()
                .filter(s -> statName.equals(s.stat().name()))
                .map(StatSlot::baseStat)
                .findFirst()
                .orElse(null);
    }
}
