package com.pokemonai.pokemon.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiClientTest {

    private MockRestServiceServer server;
    private PokeApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        // Wrap builder so MockRestServiceServer can intercept
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        RestClient.Builder interceptedBuilder = RestClient.builder(restTemplate);
        client = new PokeApiClient(interceptedBuilder, "https://pokeapi.co");
    }

    @Test
    void fetchSpeciesReturnsEnglishFlavorText() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon-species/1"))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "name": "bulbasaur",
                          "flavor_text_entries": [
                            {
                              "flavor_text": "A strange seed was\\nplanted on its back.",
                              "language": { "name": "en" }
                            },
                            {
                              "flavor_text": "Une graine mystérieuse.",
                              "language": { "name": "fr" }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonSpeciesResponse response = client.fetchSpecies(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("bulbasaur");
        assertThat(response.englishFlavorText()).isEqualTo("A strange seed was planted on its back.");
    }

    @Test
    void fetchDetailReturnsBestImageUrl() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon/1"))
                .andRespond(withSuccess("""
                        {
                          "id": 1,
                          "name": "bulbasaur",
                          "height": 7,
                          "weight": 69,
                          "types": [],
                          "stats": [],
                          "sprites": {
                            "front_default": "https://raw.githubusercontent.com/sprites/1.png",
                            "other": {
                              "official-artwork": {
                                "front_default": "https://raw.githubusercontent.com/artwork/1.png"
                              }
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonDetailResponse response = client.fetchDetail(1);

        assertThat(response.name()).isEqualTo("bulbasaur");
        assertThat(response.bestImageUrl()).isEqualTo("https://raw.githubusercontent.com/artwork/1.png");
    }

    @Test
    void fetchListReturnsParsedResults() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon?limit=151&offset=0"))
                .andRespond(withSuccess("""
                        {
                          "count": 151,
                          "results": [
                            { "name": "bulbasaur", "url": "https://pokeapi.co/api/v2/pokemon/1/" },
                            { "name": "ivysaur", "url": "https://pokeapi.co/api/v2/pokemon/2/" }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PokemonListResponse response = client.fetchList(151, 0);

        assertThat(response.count()).isEqualTo(151);
        assertThat(response.results()).hasSize(2);
        assertThat(response.results().get(0).name()).isEqualTo("bulbasaur");
    }

    @Test
    void fetchDetailWith404WrapsExceptionAsPokemonAiException() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon/999"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withResourceNotFound());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.fetchDetail(999))
                .isInstanceOf(com.pokemonai.shared.exception.PokemonAiException.class)
                .hasMessageContaining("999");
    }

    @Test
    void fetchSpeciesWith404WrapsExceptionAsPokemonAiException() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon-species/999"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withResourceNotFound());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.fetchSpecies(999))
                .isInstanceOf(com.pokemonai.shared.exception.PokemonAiException.class)
                .hasMessageContaining("999");
    }

    @Test
    void fetchDetailWith500WrapsExceptionAsPokemonAiException() {
        server.expect(requestTo("https://pokeapi.co/api/v2/pokemon/2"))
                .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
                        .withServerError());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> client.fetchDetail(2))
                .isInstanceOf(com.pokemonai.shared.exception.PokemonAiException.class)
                .hasMessageContaining("2");
    }
}
