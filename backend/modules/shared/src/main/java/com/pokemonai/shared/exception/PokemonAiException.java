package com.pokemonai.shared.exception;

public class PokemonAiException extends RuntimeException {

    public PokemonAiException(String message) {
        super(message);
    }

    public PokemonAiException(String message, Throwable cause) {
        super(message, cause);
    }
}
