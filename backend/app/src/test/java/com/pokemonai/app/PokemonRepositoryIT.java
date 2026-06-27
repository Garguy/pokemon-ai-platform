package com.pokemonai.app;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonRepositoryIT extends PostgresContainerBase {

    @Autowired
    PokemonRepository pokemonRepository;

    @BeforeEach
    void setUp() {
        pokemonRepository.deleteAll();
        for (int i = 1; i <= 10; i++) {
            pokemonRepository.save(new Pokemon(i, "pokemon-" + i, "desc-" + i, "url-" + i));
        }
    }

    @Test
    void paginationReturnsCorrectSlice() {
        Page<Pokemon> page = pokemonRepository.findAllByOrderByExternalIdAsc(PageRequest.of(0, 5));

        assertThat(page.getTotalElements()).isEqualTo(10);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getContent().get(0).getExternalId()).isEqualTo(1);
        assertThat(page.getContent().get(4).getExternalId()).isEqualTo(5);
    }

    @Test
    void secondPageReturnsNextSlice() {
        Page<Pokemon> page = pokemonRepository.findAllByOrderByExternalIdAsc(PageRequest.of(1, 5));

        assertThat(page.getContent()).hasSize(5);
        assertThat(page.getContent().get(0).getExternalId()).isEqualTo(6);
    }

    @Test
    void findByExternalIdReturnsCorrectPokemon() {
        var found = pokemonRepository.findByExternalId(7);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("pokemon-7");
    }
}
