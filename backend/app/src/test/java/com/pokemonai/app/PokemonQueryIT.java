package com.pokemonai.app;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonQueryIT extends PostgresContainerBase {

    @LocalServerPort
    int port;

    @Autowired
    PokemonRepository pokemonRepository;

    private HttpGraphQlTester tester;
    private Pokemon savedPokemon;

    @BeforeEach
    void setUp() {
        tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());
        pokemonRepository.deleteAll();
        savedPokemon = pokemonRepository.save(
                new Pokemon(1, "bulbasaur", "A strange seed.", "https://artwork/1.png"));
    }

    @Test
    void pokemonByIdReturnsCorrectData() {
        tester.document("""
                query {
                    pokemon(id: "%s") {
                        name
                        externalId
                        description
                    }
                }
                """.formatted(savedPokemon.getId()))
                .execute()
                .path("pokemon.name").entity(String.class).isEqualTo("bulbasaur")
                .path("pokemon.externalId").entity(Integer.class).isEqualTo(1)
                .path("pokemon.description").entity(String.class).isEqualTo("A strange seed.");
    }

    @Test
    void pokemonsPageReturnsAllFields() {
        tester.document("""
                query {
                    pokemons(page: 0, size: 10) {
                        totalElements
                        totalPages
                        content { name externalId }
                    }
                }
                """)
                .execute()
                .path("pokemons.totalElements").entity(Integer.class).isEqualTo(1)
                .path("pokemons.content[0].name").entity(String.class).isEqualTo("bulbasaur");
    }

    @Test
    void pokemonWithUnknownIdReturnsError() {
        tester.document("""
                query {
                    pokemon(id: "00000000-0000-0000-0000-000000000000") { name }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
