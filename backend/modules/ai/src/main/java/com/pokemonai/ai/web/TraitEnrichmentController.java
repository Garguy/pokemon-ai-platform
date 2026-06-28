package com.pokemonai.ai.web;

import com.pokemonai.ai.service.PokemonTraitBatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal/ai")
public class TraitEnrichmentController {

    private final PokemonTraitBatchService batchService;

    public TraitEnrichmentController(PokemonTraitBatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping("/extract-traits")
    public ResponseEntity<Map<String, Object>> extractTraits() {
        int enriched = batchService.enrichAll();
        return ResponseEntity.ok(Map.of("enriched", enriched));
    }
}
