package com.pokemonai.app;

import com.pokemonai.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RegisterMutationIT extends PostgresContainerBase {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Test
    void registerCreatesUserAndReturnsToken() {
        HttpGraphQlTester tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());

        tester.document("""
                mutation {
                    register(input: { email: "misty@cerulean.com", password: "securepass" }) {
                        token
                        user { id email }
                    }
                }
                """)
                .execute()
                .path("register.token").entity(String.class).satisfies(token ->
                        assertThat(token).isNotBlank())
                .path("register.user.email").entity(String.class).isEqualTo("misty@cerulean.com");

        assertThat(userRepository.existsByEmail("misty@cerulean.com")).isTrue();
    }

    @Test
    void registerWithDuplicateEmailReturnsError() {
        HttpGraphQlTester tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());

        String mutation = """
                mutation {
                    register(input: { email: "duplicate@cerulean.com", password: "securepass" }) {
                        token
                    }
                }
                """;

        // First registration succeeds
        tester.document(mutation).execute().path("register.token")
                .entity(String.class).satisfies(t -> assertThat(t).isNotBlank());

        // Second registration with same email returns an error
        tester.document(mutation).execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
