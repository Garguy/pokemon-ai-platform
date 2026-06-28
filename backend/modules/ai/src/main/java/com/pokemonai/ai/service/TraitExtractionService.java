package com.pokemonai.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemonai.recommendation.service.TraitVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class TraitExtractionService {

    private static final Logger log = LoggerFactory.getLogger(TraitExtractionService.class);

    private static final String SYSTEM_PROMPT = """
            You are a Pokémon personality analyst.
            Given a Pokémon's name and description, score each of the following six personality traits
            on a scale from 0.0 to 1.0 (inclusive). Return ONLY valid JSON with exactly these keys:
            ENERGY, CURIOSITY, LEADERSHIP, LOYALTY, RISK, CREATIVITY.
            Example: {"ENERGY":0.8,"CURIOSITY":0.6,"LEADERSHIP":0.4,"LOYALTY":0.7,"RISK":0.5,"CREATIVITY":0.3}
            Do not include any explanation, markdown, or extra text — only the JSON object.
            """;

    private static final Map<String, Double> FALLBACK = Map.of(
            "ENERGY", 0.5, "CURIOSITY", 0.5, "LEADERSHIP", 0.5,
            "LOYALTY", 0.5, "RISK", 0.5, "CREATIVITY", 0.5
    );

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public TraitExtractionService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Double> extractTraits(String pokemonName, String description) {
        String userMessage = "Pokémon: %s\nDescription: %s".formatted(pokemonName,
                description != null ? description : "No description available.");

        String raw;
        try {
            raw = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Gemini call failed for {}: {}", pokemonName, e.getMessage());
            return new LinkedHashMap<>(FALLBACK);
        }

        return parseScores(pokemonName, raw);
    }

    Map<String, Double> parseScores(String pokemonName, String raw) {
        // Strip markdown code fences if Gemini wraps the JSON anyway
        String json = raw.replaceAll("(?s)```(?:json)?\\s*", "").trim();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(json, Map.class);
            Map<String, Double> scores = new LinkedHashMap<>();
            for (String trait : TraitVector.TRAIT_NAMES) {
                Object val = parsed.get(trait);
                if (val instanceof Number n) {
                    double score = Math.min(1.0, Math.max(0.0, n.doubleValue()));
                    scores.put(trait, score);
                } else {
                    log.warn("Missing trait {} in Gemini response for {}, using 0.5", trait, pokemonName);
                    scores.put(trait, 0.5);
                }
            }
            return scores;
        } catch (Exception e) {
            log.error("Failed to parse Gemini response for {}: '{}' — using fallback", pokemonName, raw);
            return new LinkedHashMap<>(FALLBACK);
        }
    }
}
