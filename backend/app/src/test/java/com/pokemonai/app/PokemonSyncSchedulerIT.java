package com.pokemonai.app;

import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.pokemon.service.PokemonSyncScheduler;
import com.pokemonai.pokemon.service.PokemonSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokemonSyncSchedulerIT extends PostgresContainerBase {

    @MockitoBean
    PokemonSyncService pokemonSyncService;

    @Autowired
    PokemonSyncScheduler pokemonSyncScheduler;

    @Autowired
    PokemonRepository pokemonRepository;

    @Test
    void syncOnStartupTriggersWhenTableEmpty() {
        pokemonRepository.deleteAll();

        pokemonSyncScheduler.syncOnStartupIfEmpty();

        verify(pokemonSyncService).syncAll();
    }

    @Test
    void syncOnStartupSkipsWhenTableHasData() {
        pokemonRepository.deleteAll();
        pokemonRepository.save(new com.pokemonai.pokemon.domain.Pokemon(1, "bulbasaur", "desc", "url"));
        reset(pokemonSyncService);

        pokemonSyncScheduler.syncOnStartupIfEmpty();

        verifyNoInteractions(pokemonSyncService);
    }
}
