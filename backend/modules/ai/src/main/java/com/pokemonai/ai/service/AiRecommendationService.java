package com.pokemonai.ai.service;

import tools.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
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
            identify the single best-matching Gen 1 Pokémon (from the original 151) and
            4 others that partially fit.

            Rules:
            - The #1 match should be a near-perfect fit — score it 0.95–1.0.
            - Matches #2–5 should genuinely be lesser fits — score them 0.60–0.85,
              spread out. Do NOT give everyone 1.0 or similar scores.
            - Choose Pokémon whose actual personality, lore, and behaviour match
              the user's scores. Be specific and avoid generic choices.
            - The explanation for #1 should be 2–3 sentences, personal and specific,
              referencing the user's strongest traits by name.
            - Explanations for #2–5 should be 1 sentence each.
            - Plain text only — no markdown, no asterisks, no bullet points.

            Return ONLY a valid JSON array of exactly 5 objects in ranked order.
            Example format (use real content, not these placeholders):
            [
              {"name": "charizard", "score": 0.97, "explanation": "Your high energy and leadership make you a natural Charizard — always pushing forward and inspiring others."},
              {"name": "arcanine", "score": 0.78, "explanation": "Your loyalty runs deep, just like Arcanine's devotion to its trainer."}
            ]
            No markdown fences, no extra text — only the JSON array.
            """;

    public record RankedMatch(
        @JsonProperty("name") String name,
        @JsonProperty("score") double score,
        @JsonProperty("explanation") String explanation
    ) {}

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiRecommendationService(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public List<RankedMatch> rankPokemon(double energy, double curiosity, double leadership,
                                         double loyalty, double risk, double creativity) {
        String userMessage = """
                User personality scores (0.0–1.0):
                  Energy: %.2f
                  Curiosity: %.2f
                  Leadership: %.2f
                  Loyalty: %.2f
                  Risk-taking: %.2f
                  Creativity: %.2f
                Which 5 Gen 1 Pokémon best match this personality? Return the JSON array only.
                """.formatted(energy, curiosity, leadership, loyalty, risk, creativity);

        String raw;
        try {
            raw = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            if (msg.contains("429") || msg.contains("quota") || msg.toLowerCase().contains("rate")) {
                throw new IllegalStateException("AI rate limit hit — please try again in a moment", e);
            }
            throw new IllegalStateException("AI service error: " + msg, e);
        }

        return parse(raw);
    }

    List<RankedMatch> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty response from Gemini");
        }
        String json = raw.replaceAll("(?s)```(?:json)?\\s*", "").trim();
        try {
            List<RankedMatch> matches = objectMapper.readValue(json, new TypeReference<>() {});
            if (matches == null || matches.isEmpty()) {
                throw new IllegalStateException("Gemini returned an empty list");
            }
            return matches.stream().limit(5).toList();
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: '{}'", raw);
            throw new IllegalStateException("Could not parse Pokémon recommendations from AI response", e);
        }
    }
}
