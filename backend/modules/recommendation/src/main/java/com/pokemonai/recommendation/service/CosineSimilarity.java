package com.pokemonai.recommendation.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class CosineSimilarity {

    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    private CosineSimilarity() {}

    public static BigDecimal compute(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vector length mismatch");

        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot   += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) return BigDecimal.ZERO;

        double result = dot / (Math.sqrt(normA) * Math.sqrt(normB));
        // Clamp to [0, 1] to guard against floating-point drift above 1.0
        result = Math.min(1.0, Math.max(0.0, result));
        return new BigDecimal(result, MC).setScale(6, RoundingMode.HALF_UP);
    }
}
