package com.pokemonai.ai.service;

import tools.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(AiRecommendationService.class);

    private static final String SYSTEM_PROMPT = """
            You are a Pokémon personality matchmaker. Given a user's personality scores,
            identify the single best-matching Gen 1 Pokémon (from the original 151) and
            9 others that partially fit.

            Rules:
            - You will be given the list of available Pokémon. Choose ONLY from that list
              and copy the names EXACTLY as written (all lowercase).
            - Consider the FULL roster across every type — water, grass, bug, normal, rock,
              poison, ground, ice, etc. — not just the famous fire/electric/dragon ones.
              A calm, adaptable, or nurturing personality should map to water or grass types
              just as readily as a bold one maps to fire. Do not default to the same popular
              picks every time.
            - The #1 match should be a near-perfect fit — score it 0.95–1.0.
            - Matches #2–10 should genuinely be lesser fits — score them 0.55–0.85,
              spread out and gradually decreasing. Do NOT give everyone 1.0 or similar scores.
            - Choose Pokémon whose actual personality, lore, and behaviour match
              the user's scores. Be specific and avoid generic choices.
            - The explanation for #1 should be 2–3 sentences, personal and specific,
              referencing the user's strongest traits by name.
            - Explanations for #2–10 should be 1 sentence each.
            - Plain text only — no markdown, no asterisks, no bullet points.

            Return ONLY a valid JSON array of exactly 10 objects in ranked order.
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
                                         double loyalty, double risk, double creativity,
                                         List<String> availablePokemon) {
        String userMessage = """
                User personality scores (0.0–1.0):
                  Energy: %.2f
                  Curiosity: %.2f
                  Leadership: %.2f
                  Loyalty: %.2f
                  Risk-taking: %.2f
                  Creativity: %.2f

                Available Pokémon (choose only from these, exact names):
                %s

                Which 10 of these Pokémon best match this personality? Make sure the
                selection reflects a genuine range of types, not only the obvious ones.
                Return the JSON array only.
                """.formatted(energy, curiosity, leadership, loyalty, risk, creativity,
                        String.join(", ", availablePokemon));

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
            throw new IllegalStateException("Empty response from the model");
        }
        String json = raw.replaceAll("(?s)```(?:json)?", "").trim();
        int start = json.indexOf('[');
        if (start > 0) {
            json = json.substring(start);
        }

        // Happy path: well-formed array.
        try {
            List<RankedMatch> matches = objectMapper.readValue(json, new TypeReference<>() {});
            if (matches != null && !matches.isEmpty()) {
                return matches.stream().limit(10).toList();
            }
        } catch (Exception ignored) {
            // Fall through — models occasionally drop a closing brace or add a stray bracket.
        }

        // Lenient salvage: pull out individual objects, repairing a final unclosed one.
        List<RankedMatch> salvaged = salvageObjects(json);
        if (salvaged.isEmpty()) {
            log.error("Failed to parse model response: '{}'", raw);
            throw new IllegalStateException("Could not parse Pokémon recommendations from AI response");
        }
        return salvaged.stream().limit(10).toList();
    }

    private List<RankedMatch> salvageObjects(String json) {
        List<RankedMatch> result = new ArrayList<>();
        int depth = 0;
        int objStart = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (c == '\\') escaped = true;
                else if (c == '"') inString = false;
                continue;
            }
            switch (c) {
                case '"' -> inString = true;
                case '{' -> { if (depth == 0) objStart = i; depth++; }
                case '}' -> {
                    depth--;
                    if (depth == 0 && objStart >= 0) {
                        tryAdd(json.substring(objStart, i + 1), result);
                        objStart = -1;
                    }
                }
                default -> { /* ignore */ }
            }
        }
        // A trailing object the model started but never closed (dropped the final brace).
        if (depth > 0 && objStart >= 0) {
            String tail = json.substring(objStart).trim();
            while (tail.endsWith("]") || tail.endsWith(",")) {
                tail = tail.substring(0, tail.length() - 1).trim();
            }
            tryAdd(tail + "}", result);
        }
        return result;
    }

    private void tryAdd(String objectJson, List<RankedMatch> out) {
        try {
            out.add(objectMapper.readValue(objectJson, RankedMatch.class));
        } catch (Exception ignored) {
            // Skip an object we still can't parse.
        }
    }
}
