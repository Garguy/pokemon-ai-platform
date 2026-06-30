package com.pokemonai.app.graphql;

import com.pokemonai.pokemon.domain.Pokemon;

import java.util.HashMap;
import java.util.Map;

/** Builds the GraphQL {@code Pokemon} response shape from the entity. */
final class PokemonMaps {

    private PokemonMaps() {}

    static Map<String, Object> toMap(Pokemon p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId().toString());
        map.put("externalId", p.getExternalId());
        map.put("name", p.getName());
        map.put("description", p.getDescription() != null ? p.getDescription() : "");
        map.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
        map.put("syncedAt", p.getSyncedAt().toString());
        map.put("types", p.getTypes());
        map.put("height", p.getHeight());
        map.put("weight", p.getWeight());
        map.put("category", p.getCategory());
        map.put("hp", p.getHp());
        map.put("attack", p.getAttack());
        map.put("defense", p.getDefense());
        map.put("specialAttack", p.getSpecialAttack());
        map.put("specialDefense", p.getSpecialDefense());
        map.put("speed", p.getSpeed());
        return map;
    }
}
