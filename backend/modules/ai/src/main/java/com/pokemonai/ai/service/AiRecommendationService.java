package com.pokemonai.ai.service;

import tools.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class AiRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AiRecommendationService.class);

    private static final String SYSTEM_PROMPT = """
            You are a Pokémon personality matchmaker. Given a user's personality scores,
            return the 5 Gen 1 Pokémon (from the original 151) that best match their personality.
            Return ONLY a valid JSON array of exactly 5 lowercase Pokémon names in ranked order.
            Example: ["pikachu","eevee","snorlax","gengar","mewtwo"]
            Do not include any explanation, markdown, or extra text — only the JSON array.
            """;

    private static final List<String> FALLBACK = List.of("pikachu", "eevee", "snorlax", "gengar", "mewtwo");

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiRecommendationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<String> rankPokemon(double energy, double curiosity, double leadership,
                                    double loyalty, double risk, double creativity) {
        String userMessage = """
                User personality scores (0.0–1.0):
                  Energy: %.2f
                  Curiosity: %.2f
                  Leadership: %.2f
                  Loyalty: %.2f
                  Risk-taking: %.2f
                  Creativity: %.2f
                Which 5 Gen 1 Pokémon best match this personality? Return only the JSON array.
                """.formatted(energy, curiosity, leadership, loyalty, risk, creativity);

        String raw;
        try {
            raw = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Gemini recommendation call failed: {}", e.getMessage());
            return FALLBACK;
        }

        return parse(raw);
    }

    List<String> parse(String raw) {
        String json = raw.replaceAll("(?s)```(?:json)?\\s*", "").trim();
        try {
            List<String> names = objectMapper.readValue(json, new TypeReference<>() {});
            if (names == null || names.isEmpty()) return FALLBACK;
            return names.stream().map(String::toLowerCase).limit(5).toList();
        } catch (Exception e) {
            log.error("Failed to parse Gemini recommendation response: '{}' — using fallback", raw);
            return FALLBACK;
        }
    }
}
