package com.pokemonai.ai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class RecommendationExplainer {

    private static final Logger log = LoggerFactory.getLogger(RecommendationExplainer.class);

    private static final String SYSTEM_PROMPT = """
            You are a friendly Pokémon personality matchmaker.
            In 2–3 sentences, explain why a person with the given personality traits
            is matched with the given Pokémon. Be specific about which traits align.
            Keep the tone warm and engaging. Do not use lists or bullet points.
            """;

    private final ChatClient chatClient;

    public RecommendationExplainer(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String explain(String pokemonName, String pokemonDescription,
                          double energy, double curiosity, double leadership,
                          double loyalty, double risk, double creativity) {
        String userMessage = """
                Pokémon: %s
                Pokémon description: %s
                User personality scores (0.0–1.0):
                  Energy: %.2f, Curiosity: %.2f, Leadership: %.2f
                  Loyalty: %.2f, Risk: %.2f, Creativity: %.2f
                """.formatted(pokemonName, pokemonDescription != null ? pokemonDescription : "Unknown",
                energy, curiosity, leadership, loyalty, risk, creativity);

        try {
            return chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userMessage)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Explanation generation failed for {}: {}", pokemonName, e.getMessage());
            return "Your personality aligns with %s in several key ways.".formatted(pokemonName);
        }
    }
}
