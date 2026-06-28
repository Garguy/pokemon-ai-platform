package com.pokemonai.app;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.recommendation.domain.PokemonTrait;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonTraitRepositoryIT extends PostgresContainerBase {

    @Autowired PokemonRepository pokemonRepository;
    @Autowired PokemonTraitRepository pokemonTraitRepository;

    private Pokemon savedPokemon;

    @BeforeEach
    void setUp() {
        pokemonTraitRepository.deleteAll();
        pokemonRepository.deleteAll();
        savedPokemon = pokemonRepository.save(new Pokemon(1, "bulbasaur", "desc", "url"));
    }

    @Test
    void findByPokemonIdReturnsSixTraits() {
        insertTraits(savedPokemon);

        List<PokemonTrait> traits = pokemonTraitRepository.findByIdPokemonId(savedPokemon.getId());

        assertThat(traits).hasSize(6);
    }

    @Test
    void findAllOrderedGroupsByPokemon() {
        Pokemon ivysaur = pokemonRepository.save(new Pokemon(2, "ivysaur", "desc2", "url2"));
        insertTraits(savedPokemon);
        insertTraits(ivysaur);

        List<PokemonTrait> all = pokemonTraitRepository.findAllOrderedByPokemon();

        assertThat(all).hasSize(12);
        // All traits for each pokemon are consecutive
        long bulbasaurCount = all.stream()
                .filter(t -> t.getPokemonId().equals(savedPokemon.getId()))
                .count();
        assertThat(bulbasaurCount).isEqualTo(6);
    }

    private void insertTraits(Pokemon pokemon) {
        String[] traits = {"ENERGY","CURIOSITY","LEADERSHIP","LOYALTY","RISK","CREATIVITY"};
        for (String trait : traits) {
            pokemonTraitRepository.save(new PokemonTrait(pokemon.getId(), trait, new BigDecimal("0.5000")));
        }
    }
}
