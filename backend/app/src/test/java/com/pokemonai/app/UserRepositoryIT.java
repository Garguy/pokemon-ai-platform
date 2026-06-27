package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRepositoryIT extends PostgresContainerBase {

    @Autowired
    UserRepository userRepository;

    @Test
    void saveAndFindByEmail() {
        User user = new User("ash@pokemon.com", "hashed");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("ash@pokemon.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("ash@pokemon.com");
        assertThat(found.get().getId()).isNotNull();
    }
}
