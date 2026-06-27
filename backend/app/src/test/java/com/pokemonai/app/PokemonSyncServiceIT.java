package com.pokemonai.app;

import com.pokemonai.pokemon.client.PokeApiClient;
import com.pokemonai.pokemon.client.PokemonDetailResponse;
import com.pokemonai.pokemon.client.PokemonSpeciesResponse;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.pokemon.service.PokemonSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonSyncServiceIT extends PostgresContainerBase {

    @MockitoBean
    PokeApiClient pokeApiClient;

    @Autowired
    PokemonSyncService pokemonSyncService;

    @Autowired
    PokemonRepository pokemonRepository;

    @BeforeEach
    void setUp() {
        pokemonRepository.deleteAll();
    }

    @Test
    void syncOnePersistsPokemonRow() {
        when(pokeApiClient.fetchDetail(1)).thenReturn(new PokemonDetailResponse(
                1, "bulbasaur",
                new PokemonDetailResponse.Sprites(
                        "https://sprites/1.png",
                        new PokemonDetailResponse.Other(
                                new PokemonDetailResponse.OfficialArtwork("https://artwork/1.png")))));

        when(pokeApiClient.fetchSpecies(1)).thenReturn(new PokemonSpeciesResponse(
                1, "bulbasaur",
                List.of(new PokemonSpeciesResponse.FlavorTextEntry(
                        "A strange seed.",
                        new PokemonSpeciesResponse.Language("en")))));

        pokemonSyncService.syncOne(1);

        Optional<Pokemon> found = pokemonRepository.findByExternalId(1);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("bulbasaur");
        assertThat(found.get().getDescription()).isEqualTo("A strange seed.");
        assertThat(found.get().getImageUrl()).isEqualTo("https://artwork/1.png");
    }

    @Test
    void syncOneUpdatesExistingRow() {
        pokemonRepository.save(new Pokemon(1, "bulbasaur", "Old desc", "old-url"));

        when(pokeApiClient.fetchDetail(1)).thenReturn(new PokemonDetailResponse(
                1, "bulbasaur",
                new PokemonDetailResponse.Sprites("https://sprites/1.png",
                        new PokemonDetailResponse.Other(
                                new PokemonDetailResponse.OfficialArtwork("https://new-artwork/1.png")))));

        when(pokeApiClient.fetchSpecies(1)).thenReturn(new PokemonSpeciesResponse(
                1, "bulbasaur",
                List.of(new PokemonSpeciesResponse.FlavorTextEntry(
                        "New desc.", new PokemonSpeciesResponse.Language("en")))));

        pokemonSyncService.syncOne(1);

        assertThat(pokemonRepository.count()).isEqualTo(1);
        Optional<Pokemon> updated = pokemonRepository.findByExternalId(1);
        assertThat(updated.get().getDescription()).isEqualTo("New desc.");
        assertThat(updated.get().getImageUrl()).isEqualTo("https://new-artwork/1.png");
    }
}
