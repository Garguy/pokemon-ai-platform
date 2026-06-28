package com.pokemonai.recommendation.service;

/**
 * Fixed trait ordering for cosine similarity — must match TraitCategory enum declaration order.
 */
public final class TraitVector {

    public static final String[] TRAIT_NAMES = {
            "ENERGY", "CURIOSITY", "LEADERSHIP", "LOYALTY", "RISK", "CREATIVITY"
    };

    private TraitVector() {}
}
