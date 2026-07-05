package com.pokemonai.identity.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class JwtSecretValidator {

    private final String secret;
    private final ApplicationContext context;

    public JwtSecretValidator(
            @Value("${app.jwt.secret}") String secret,
            ApplicationContext context) {
        this.secret = secret;
        this.context = context;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void validate() {
        if (secret == null || secret.isBlank()) {
            byte[] bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            String generated = Base64.getEncoder().encodeToString(bytes);
            System.err.println();
            System.err.println("==========================================================");
            System.err.println("  JWT_SECRET is not set.");
            System.err.println("  Add the following to your backend/.env file:");
            System.err.println();
            System.err.println("  JWT_SECRET=" + generated);
            System.err.println();
            System.err.println("  Then restart the application.");
            System.err.println("==========================================================");
            System.err.println();
            SpringApplication.exit(context, () -> 1);
            System.exit(1);
        }
    }
}
