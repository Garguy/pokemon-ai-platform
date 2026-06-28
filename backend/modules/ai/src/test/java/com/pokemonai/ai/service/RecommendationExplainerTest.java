package com.pokemonai.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationExplainerTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;
    @Mock ChatClient.ChatClientRequestSpec systemSpec;
    @Mock ChatClient.ChatClientRequestSpec userSpec;
    @Mock ChatClient.CallResponseSpec callSpec;

    RecommendationExplainer explainer;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        explainer = new RecommendationExplainer(chatClientBuilder);
    }

    @Test
    void explainReturnsChatClientResponse() {
        String expected = "Pikachu matches your high energy and risk-taking personality perfectly.";

        when(chatClient.prompt()).thenReturn(systemSpec);
        when(systemSpec.system(anyString())).thenReturn(systemSpec);
        when(systemSpec.user(anyString())).thenReturn(systemSpec);
        when(systemSpec.call()).thenReturn(callSpec);
        when(callSpec.content()).thenReturn(expected);

        String result = explainer.explain("pikachu", "An electric mouse.",
                0.9, 0.5, 0.4, 0.3, 0.8, 0.5);

        assertThat(result).isEqualTo(expected);
        assertThat(result).isNotBlank();
    }

    @Test
    void explainReturnsFallbackOnException() {
        when(chatClient.prompt()).thenThrow(new RuntimeException("API down"));

        String result = explainer.explain("pikachu", "An electric mouse.",
                0.9, 0.5, 0.4, 0.3, 0.8, 0.5);

        assertThat(result).isNotBlank();
        assertThat(result).contains("pikachu");
    }
}
