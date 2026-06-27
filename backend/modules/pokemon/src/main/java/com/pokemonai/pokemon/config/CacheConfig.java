package com.pokemonai.pokemon.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CaffeineCache pokemonByIdCache() {
        return new CaffeineCache("pokemon-by-id",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .build());
    }

    @Bean
    public CaffeineCache pokemonPageCache() {
        return new CaffeineCache("pokemon-page",
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(1, TimeUnit.HOURS)
                        .build());
    }
}
