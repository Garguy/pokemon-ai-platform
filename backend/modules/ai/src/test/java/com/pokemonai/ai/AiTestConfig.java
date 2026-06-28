package com.pokemonai.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokemonai.ai.service.TraitExtractionService;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class AiTestConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public TraitExtractionService traitExtractionService(
            org.springframework.ai.chat.client.ChatClient.Builder builder,
            ObjectMapper objectMapper) {
        return new TraitExtractionService(builder, objectMapper);
    }
}
