package com.pokemonai.app;

import com.pokemonai.ai.service.PokemonTraitBatchService;
import com.pokemonai.ai.service.TraitExtractionService;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonTraitBatchServiceIT extends PostgresContainerBase {

    @MockitoBean
    TraitExtractionService traitExtractionService;

    @Autowired PokemonTraitBatchService batchService;
    @Autowired PokemonRepository pokemonRepository;
    @Autowired PokemonTraitRepository traitRepository;

    @BeforeEach
    void setUp() {
        traitRepository.deleteAll();
        pokemonRepository.deleteAll();

        pokemonRepository.save(new Pokemon(1, "bulbasaur", "A grass Pokémon.", "url1"));
        pokemonRepository.save(new Pokemon(2, "charmander", "A fire Pokémon.", "url2"));
        pokemonRepository.save(new Pokemon(3, "squirtle", "A water Pokémon.", "url3"));

        when(traitExtractionService.extractTraits(anyString(), anyString()))
                .thenAnswer(inv -> {
                    String name = inv.getArgument(0);
                    // Give each pokemon a distinct score based on name length for assertability
                    double score = name.length() * 0.05;
                    return Map.of(
                            "ENERGY", score, "CURIOSITY", score,
                            "LEADERSHIP", score, "LOYALTY", score,
                            "RISK", score, "CREATIVITY", score
                    );
                });
    }

    @Test
    void enrichAllUpdatesTraitScoresFromPlaceholder() {
        int enriched = batchService.enrichAll();

        assertThat(enriched).isEqualTo(3);

        // All 3 Pokemon should have 6 traits each
        assertThat(traitRepository.count()).isEqualTo(18);
    }

    @Test
    void enrichAllSetsDistinctScoresPerPokemon() {
        batchService.enrichAll();

        List<com.pokemonai.recommendation.domain.PokemonTrait> bulbasaurTraits = traitRepository
                .findByIdPokemonId(pokemonRepository.findAll().stream()
                        .filter(p -> p.getName().equals("bulbasaur"))
                        .findFirst().orElseThrow().getId());

        // bulbasaur has 9 chars → score = 0.45
        bulbasaurTraits.forEach(t ->
                assertThat(t.getTraitScore().doubleValue()).isEqualTo(0.45, 
                        org.assertj.core.api.Assertions.within(0.0001)));
    }
}
