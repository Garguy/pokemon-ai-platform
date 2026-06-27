package com.pokemonai.pokemon.service;

import com.pokemonai.pokemon.client.PokeApiClient;
import com.pokemonai.pokemon.client.PokemonDetailResponse;
import com.pokemonai.pokemon.client.PokemonSpeciesResponse;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PokemonSyncService {

    private static final Logger log = LoggerFactory.getLogger(PokemonSyncService.class);
    private static final int GEN1_LIMIT = 151;

    private final PokeApiClient pokeApiClient;
    private final PokemonRepository pokemonRepository;

    public PokemonSyncService(PokeApiClient pokeApiClient, PokemonRepository pokemonRepository) {
        this.pokeApiClient = pokeApiClient;
        this.pokemonRepository = pokemonRepository;
    }

    public void syncAll() {
        log.info("Starting full Pokemon sync for Gen 1 ({} pokemon)", GEN1_LIMIT);
        int synced = 0;
        int failed = 0;
        for (int id = 1; id <= GEN1_LIMIT; id++) {
            try {
                syncOne(id);
                synced++;
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sync interrupted at id={}", id);
                break;
            } catch (Exception e) {
                log.error("Failed to sync pokemon id={}", id, e);
                failed++;
            }
        }
        log.info("Pokemon sync complete. synced={} failed={}", synced, failed);
    }

    @Transactional
    public void syncOne(int externalId) {
        PokemonDetailResponse detail = pokeApiClient.fetchDetail(externalId);
        PokemonSpeciesResponse species = pokeApiClient.fetchSpecies(externalId);

        String description = species.englishFlavorText();
        String imageUrl = detail.bestImageUrl();

        Optional<Pokemon> existing = pokemonRepository.findByExternalId(externalId);
        if (existing.isPresent()) {
            existing.get().update(detail.name(), description, imageUrl);
        } else {
            pokemonRepository.save(new Pokemon(externalId, detail.name(), description, imageUrl));
        }
    }
}
