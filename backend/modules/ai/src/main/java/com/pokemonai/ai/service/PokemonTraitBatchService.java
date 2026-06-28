package com.pokemonai.ai.service;

import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.recommendation.domain.PokemonTrait;
import com.pokemonai.recommendation.domain.PokemonTraitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
public class PokemonTraitBatchService {

    private static final Logger log = LoggerFactory.getLogger(PokemonTraitBatchService.class);
    // 1 request/second to stay well inside the 15 RPM free tier limit
    private static final long RATE_LIMIT_MS = 1000;

    private final PokemonRepository pokemonRepository;
    private final PokemonTraitRepository traitRepository;
    private final TraitExtractionService traitExtractionService;

    // Self-injection via proxy so enrichOne's @Transactional runs in its own transaction,
    // isolating per-Pokémon failures from each other.
    @Lazy
    @Autowired
    private PokemonTraitBatchService self;

    public PokemonTraitBatchService(PokemonRepository pokemonRepository,
                                     PokemonTraitRepository traitRepository,
                                     TraitExtractionService traitExtractionService) {
        this.pokemonRepository = pokemonRepository;
        this.traitRepository = traitRepository;
        this.traitExtractionService = traitExtractionService;
    }

    public int enrichAll() {
        List<Pokemon> allPokemon = pokemonRepository.findAll();
        int enriched = 0;

        for (Pokemon pokemon : allPokemon) {
            try {
                self.enrichOne(pokemon);
                enriched++;
                Thread.sleep(RATE_LIMIT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Trait enrichment interrupted at {}", pokemon.getName());
                break;
            } catch (Exception e) {
                log.error("Failed to enrich traits for {}: {}", pokemon.getName(), e.getMessage());
            }
        }

        log.info("Trait enrichment complete: {}/{} pokemon enriched", enriched, allPokemon.size());
        return enriched;
    }

    @Transactional
    public void enrichOne(Pokemon pokemon) {
        Map<String, Double> scores = traitExtractionService.extractTraits(
                pokemon.getName(), pokemon.getDescription());

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            BigDecimal score = BigDecimal.valueOf(entry.getValue())
                    .setScale(4, RoundingMode.HALF_UP);
            traitRepository.save(new PokemonTrait(pokemon.getId(), entry.getKey(), score));
        }
    }
}
