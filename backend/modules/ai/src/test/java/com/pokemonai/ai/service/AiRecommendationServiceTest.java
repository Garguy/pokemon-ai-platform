package com.pokemonai.ai.service;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiRecommendationServiceTest {

    @Mock ChatClient.Builder chatClientBuilder;
    @Mock ChatClient chatClient;

    AiRecommendationService service;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        service = new AiRecommendationService(chatClientBuilder, new ObjectMapper());
    }

    @Test
    void parsesWellFormedArray() {
        String json = """
                [{"name":"charizard","score":0.97,"explanation":"a"},
                 {"name":"squirtle","score":0.7,"explanation":"b"}]
                """;

        List<AiRecommendationService.RankedMatch> matches = service.parse(json);

        assertThat(matches).hasSize(2);
        assertThat(matches.get(0).name()).isEqualTo("charizard");
    }

    @Test
    void stripsMarkdownFences() {
        String json = """
                ```json
                [{"name":"bulbasaur","score":0.9,"explanation":"a"}]
                ```
                """;

        assertThat(service.parse(json)).hasSize(1);
    }

    @Test
    void salvagesTrailingObjectMissingClosingBrace() {
        // Real failure: the final object lost its "}" and gained a stray "]".
        String malformed = "[{\"name\": \"charizard\", \"score\": 0.98, \"explanation\": \"x\"},"
                + "{\"name\": \"gyarados\", \"score\": 0.76, \"explanation\": \"y\"},"
                + "{\"name\": \"tauros\", \"score\": 0.66, \"explanation\": \"z\"]]";

        List<AiRecommendationService.RankedMatch> matches = service.parse(malformed);

        assertThat(matches).hasSize(3);
        assertThat(matches).extracting(AiRecommendationService.RankedMatch::name)
                .containsExactly("charizard", "gyarados", "tauros");
    }

    @Test
    void cappsAtTenMatches() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 14; i++) {
            if (i > 0) sb.append(",");
            sb.append("{\"name\":\"p").append(i).append("\",\"score\":0.5,\"explanation\":\"e\"}");
        }
        sb.append("]");

        assertThat(service.parse(sb.toString())).hasSize(10);
    }
}
