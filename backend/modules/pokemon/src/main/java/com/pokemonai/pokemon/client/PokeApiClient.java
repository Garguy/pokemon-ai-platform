package com.pokemonai.pokemon.client;

import com.pokemonai.shared.exception.PokemonAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PokeApiClient {

    private final RestClient restClient;

    public PokeApiClient(
            RestClient.Builder builder,
            @Value("${pokeapi.base-url:https://pokeapi.co}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Retryable(retryFor = RestClientException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public PokemonListResponse fetchList(int limit, int offset) {
        return restClient.get()
                .uri("/api/v2/pokemon?limit={limit}&offset={offset}", limit, offset)
                .retrieve()
                .body(PokemonListResponse.class);
    }

    @Retryable(retryFor = RestClientException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public PokemonSpeciesResponse fetchSpecies(int id) {
        try {
            return restClient.get()
                    .uri("/api/v2/pokemon-species/{id}", id)
                    .retrieve()
                    .body(PokemonSpeciesResponse.class);
        } catch (RestClientException e) {
            throw new PokemonAiException("Failed to fetch species for id " + id, e);
        }
    }

    @Retryable(retryFor = RestClientException.class, maxAttempts = 3,
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public PokemonDetailResponse fetchDetail(int id) {
        try {
            return restClient.get()
                    .uri("/api/v2/pokemon/{id}", id)
                    .retrieve()
                    .body(PokemonDetailResponse.class);
        } catch (RestClientException e) {
            throw new PokemonAiException("Failed to fetch detail for id " + id, e);
        }
    }
}
