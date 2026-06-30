package com.pokemonai.app.graphql;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.service.PokemonService;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class PokemonController {

    private final PokemonService pokemonService;

    public PokemonController(PokemonService pokemonService) {
        this.pokemonService = pokemonService;
    }

    @QueryMapping
    public Map<String, Object> pokemon(@Argument String id) {
        Pokemon p = pokemonService.findById(UUID.fromString(id));
        return toMap(p);
    }

    @QueryMapping
    public Map<String, Object> pokemons(@Argument int page, @Argument int size) {
        Page<Pokemon> result = pokemonService.findAll(page, size);
        List<Map<String, Object>> content = result.getContent().stream()
                .map(this::toMap)
                .toList();
        return Map.of(
                "content", content,
                "totalElements", (int) result.getTotalElements(),
                "totalPages", result.getTotalPages(),
                "page", result.getNumber(),
                "size", result.getSize()
        );
    }

    private Map<String, Object> toMap(Pokemon p) {
        return PokemonMaps.toMap(p);
    }
}
