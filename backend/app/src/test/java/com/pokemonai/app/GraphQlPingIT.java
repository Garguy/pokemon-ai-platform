package com.pokemonai.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphQlPingIT extends PostgresContainerBase {

    @LocalServerPort
    int port;

    @Test
    void pingReturnsPong() {
        HttpGraphQlTester tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());

        tester.document("{ ping }")
                .execute()
                .path("ping")
                .entity(String.class)
                .isEqualTo("pong");
    }
}
