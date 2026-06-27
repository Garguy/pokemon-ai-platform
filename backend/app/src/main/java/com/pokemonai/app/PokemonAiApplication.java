package com.pokemonai.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.pokemonai")
@AutoConfigurationPackage(basePackages = "com.pokemonai")
public class PokemonAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemonAiApplication.class, args);
    }
}
