package com.pokemonai.app;

import com.pokemonai.ai.service.AiRecommendationService;
import com.pokemonai.ai.service.RecommendationExplainer;
import com.pokemonai.ai.service.TraitExtractionService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestAiConfig {

    @Bean
    @Primary
    public AiRecommendationService aiRecommendationService() {
        AiRecommendationService mock = Mockito.mock(AiRecommendationService.class);
        // Return 5 ranked matches using names that the test seeds in the DB
        when(mock.rankPokemon(anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyList()))
                .thenAnswer(inv -> {
                    List<String> available = inv.getArgument(6);
                    return available.stream()
                            .limit(10)
                            .map(name -> new AiRecommendationService.RankedMatch(
                                    name, 0.8, "Test explanation for " + name))
                            .toList();
                });
        return mock;
    }

    @Bean
    @Primary
    public RecommendationExplainer recommendationExplainer() {
        RecommendationExplainer mock = Mockito.mock(RecommendationExplainer.class);
        when(mock.explain(anyString(), any(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .thenAnswer(inv -> "Test explanation for " + inv.getArgument(0));
        return mock;
    }

    @Bean
    @Primary
    public TraitExtractionService traitExtractionService() {
        TraitExtractionService mock = Mockito.mock(TraitExtractionService.class);
        when(mock.extractTraits(anyString(), anyString()))
                .thenReturn(Map.of(
                        "ENERGY", 0.5, "CURIOSITY", 0.5, "LEADERSHIP", 0.5,
                        "LOYALTY", 0.5, "RISK", 0.5, "CREATIVITY", 0.5));
        return mock;
    }
}
