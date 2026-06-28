package com.pokemonai.ai.service;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Real Gemini call — excluded from normal CI.
 * Run with: ./gradlew :modules:ai:test -Dtags=integration-ai
 * Requires GEMINI_API_KEY env var to be set.
 */
@Tag("integration-ai")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
@SpringBootTest(classes = com.pokemonai.ai.AiTestConfig.class)
@TestPropertySource(properties = {
        "spring.ai.google.genai.api-key=${GEMINI_API_KEY}",
        "spring.ai.google.genai.chat.model=gemini-2.5-flash",
        "spring.ai.google.genai.chat.temperature=0.2",
        "spring.ai.google.genai.chat.max-output-tokens=512",
        "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration"
})
class TraitExtractionServiceIT {

    @Autowired
    TraitExtractionService traitExtractionService;

    @Test
    void pikachuTraitsAllInRangeAndEnergyAboveHalf() {
        Map<String, Double> scores = traitExtractionService.extractTraits(
                "pikachu",
                "A small electric mouse Pokémon. It stores electricity in its cheek pouches " +
                "and releases it in lightning-fast movements. Very friendly and brave.");

        assertThat(scores).containsKeys("ENERGY","CURIOSITY","LEADERSHIP","LOYALTY","RISK","CREATIVITY");
        scores.values().forEach(v ->
                assertThat(v).isBetween(0.0, 1.0));
        assertThat(scores.get("ENERGY")).isGreaterThan(0.5);
    }
}
