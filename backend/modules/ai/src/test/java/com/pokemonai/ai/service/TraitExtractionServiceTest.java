package com.pokemonai.ai.service;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraitExtractionServiceTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec requestSpec;
    @Mock ChatClient.CallResponseSpec callSpec;

    TraitExtractionService service;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new TraitExtractionService(chatClientBuilder, new ObjectMapper());
    }

    @Test
    void cleanJsonResponseParsesCorrectly() {
        String json = """
                {"ENERGY":0.8,"CURIOSITY":0.6,"LEADERSHIP":0.4,"LOYALTY":0.7,"RISK":0.5,"CREATIVITY":0.3}
                """;

        Map<String, Double> scores = service.parseScores("pikachu", json.trim());

        assertThat(scores).containsEntry("ENERGY", 0.8)
                          .containsEntry("CURIOSITY", 0.6)
                          .containsEntry("LEADERSHIP", 0.4)
                          .containsEntry("LOYALTY", 0.7)
                          .containsEntry("RISK", 0.5)
                          .containsEntry("CREATIVITY", 0.3);
    }

    @Test
    void markdownWrappedJsonStillParses() {
        String wrapped = """
                ```json
                {"ENERGY":0.9,"CURIOSITY":0.7,"LEADERSHIP":0.6,"LOYALTY":0.5,"RISK":0.8,"CREATIVITY":0.4}
                ```""";

        Map<String, Double> scores = service.parseScores("charizard", wrapped);

        assertThat(scores).containsEntry("ENERGY", 0.9)
                          .containsEntry("RISK", 0.8);
    }

    @Test
    void invalidJsonReturnsFallbackAllPointFive() {
        Map<String, Double> scores = service.parseScores("unknown", "not json at all");

        assertThat(scores).hasSize(6);
        scores.values().forEach(v -> assertThat(v).isEqualTo(0.5));
    }

    @Test
    void scoresAreClamped() {
        String json = """
                {"ENERGY":1.5,"CURIOSITY":-0.2,"LEADERSHIP":0.5,"LOYALTY":0.5,"RISK":0.5,"CREATIVITY":0.5}
                """;

        Map<String, Double> scores = service.parseScores("test", json.trim());

        assertThat(scores.get("ENERGY")).isEqualTo(1.0);
        assertThat(scores.get("CURIOSITY")).isEqualTo(0.0);
    }

    @Test
    void missingTraitKeyFallsBackToPointFive() {
        // Missing CREATIVITY key
        String json = """
                {"ENERGY":0.8,"CURIOSITY":0.6,"LEADERSHIP":0.4,"LOYALTY":0.7,"RISK":0.5}
                """;

        Map<String, Double> scores = service.parseScores("test", json.trim());

        assertThat(scores.get("CREATIVITY")).isEqualTo(0.5);
    }
}
