package com.pokemonai.app;

import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.pokemon.service.PokemonSyncScheduler;
import com.pokemonai.pokemon.service.PokemonSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.mockito.Mockito;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonSyncSchedulerIT extends PostgresContainerBase {

    @MockitoBean
    PokemonSyncService pokemonSyncService;

    @Autowired
    PokemonSyncScheduler pokemonSyncScheduler;

    @Autowired
    PokemonRepository pokemonRepository;

    @BeforeEach
    void setUp() {
        pokemonRepository.deleteAll();
        // Reset interactions recorded during Spring context startup (ApplicationReadyEvent)
        Mockito.reset(pokemonSyncService);
    }

    @Test
    void syncOnStartupTriggersWhenTableEmpty() {
        pokemonSyncScheduler.syncOnStartupIfNeeded();

        verify(pokemonSyncService).syncAll();
    }

    @Test
    void syncOnStartupSkipsWhenTableHasData() {
        com.pokemonai.pokemon.domain.Pokemon p =
                new com.pokemonai.pokemon.domain.Pokemon(1, "bulbasaur", "desc", "url");
        // applyDetails sets typesCsv — without it existsByTypesCsvIsNull() triggers a backfill
        p.applyDetails(java.util.List.of("grass"), 7, 69, "Seed", 45, 49, 49, 65, 65, 45);
        pokemonRepository.save(p);
        reset(pokemonSyncService);

        pokemonSyncScheduler.syncOnStartupIfNeeded();

        verifyNoInteractions(pokemonSyncService);
    }
}
