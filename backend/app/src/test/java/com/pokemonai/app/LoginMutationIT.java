package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginMutationIT extends PostgresContainerBase {

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private HttpGraphQlTester tester;

    @BeforeEach
    void setUp() {
        tester = HttpGraphQlTester.create(
                WebTestClient.bindToServer()
                        .baseUrl("http://localhost:" + port + "/graphql")
                        .build());
        userRepository.deleteAll();
        userRepository.save(new User("brock@pokemon.com", passwordEncoder.encode("rocksolid")));
    }

    @Test
    void loginWithCorrectCredentialsReturnsToken() {
        tester.document("""
                mutation {
                    login(input: { email: "brock@pokemon.com", password: "rocksolid" }) {
                        token
                        user { email }
                    }
                }
                """)
                .execute()
                .path("login.token").entity(String.class).satisfies(token ->
                        assertThat(token).isNotBlank())
                .path("login.user.email").entity(String.class).isEqualTo("brock@pokemon.com");
    }

    @Test
    void loginWithWrongPasswordReturnsError() {
        tester.document("""
                mutation {
                    login(input: { email: "brock@pokemon.com", password: "wrongpass" }) {
                        token
                    }
                }
                """)
                .execute()
                .errors()
                .satisfy(errors -> assertThat(errors).isNotEmpty());
    }
}
