package com.pokemonai.shared.exception;

public class ResourceNotFoundException extends PokemonAiException {

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found: " + id);
    }
}
