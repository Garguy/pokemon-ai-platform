package com.pokemonai.pokemon.service;

import com.pokemonai.pokemon.domain.PokemonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PokemonSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(PokemonSyncScheduler.class);

    private final PokemonSyncService syncService;
    private final PokemonRepository pokemonRepository;

    public PokemonSyncScheduler(PokemonSyncService syncService, PokemonRepository pokemonRepository) {
        this.syncService = syncService;
        this.pokemonRepository = pokemonRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncOnStartupIfNeeded() {
        if (pokemonRepository.count() == 0) {
            log.info("Pokemon table is empty — triggering initial sync");
            syncService.syncAll();
        } else if (pokemonRepository.existsByTypesCsvIsNull()) {
            log.info("Pokemon rows missing detail fields — triggering backfill sync");
            syncService.syncAll();
        }
    }

    // Every Sunday at 03:00
    @Scheduled(cron = "0 0 3 * * SUN")
    public void weeklySync() {
        log.info("Weekly Pokemon sync triggered");
        syncService.syncAll();
    }
}
