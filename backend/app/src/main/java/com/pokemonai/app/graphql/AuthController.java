package com.pokemonai.app.graphql;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @MutationMapping
    public Map<String, Object> register(@Argument Map<String, String> input) {
        var result = userService.register(input.get("email"), input.get("password"));
        return toPayload(result);
    }

    @MutationMapping
    public Map<String, Object> login(@Argument Map<String, String> input) {
        var result = userService.login(input.get("email"), input.get("password"));
        return toPayload(result);
    }

    @QueryMapping
    public Map<String, Object> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByEmail((String) authentication.getPrincipal());
        return toUserMap(user);
    }

    private Map<String, Object> toPayload(UserService.AuthResult result) {
        return Map.of(
                "token", result.token(),
                "user", toUserMap(result.user())
        );
    }

    private Map<String, Object> toUserMap(User user) {
        return Map.of(
                "id", user.getId().toString(),
                "email", user.getEmail(),
                "createdAt", user.getCreatedAt().toString()
        );
    }
}
