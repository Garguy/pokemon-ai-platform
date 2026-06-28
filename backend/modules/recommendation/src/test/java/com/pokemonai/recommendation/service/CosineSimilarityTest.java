package com.pokemonai.recommendation.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CosineSimilarityTest {

    @Test
    void identicalVectorsReturnOne() {
        double[] v = {0.8, 0.6, 0.4, 0.9, 0.3, 0.7};
        assertThat(CosineSimilarity.compute(v, v))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void orthogonalVectorsReturnZero() {
        double[] a = {1.0, 0.0, 0.0};
        double[] b = {0.0, 1.0, 0.0};
        assertThat(CosineSimilarity.compute(a, b))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void zeroVectorReturnsZeroNotException() {
        double[] zero = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double[] other = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
        assertThat(CosineSimilarity.compute(zero, other))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void zeroVsZeroReturnsZero() {
        double[] zero = {0.0, 0.0, 0.0};
        assertThat(CosineSimilarity.compute(zero, zero))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void partialOverlapIsInRangeZeroToOne() {
        double[] a = {1.0, 0.5, 0.0};
        double[] b = {0.5, 1.0, 0.0};
        BigDecimal result = CosineSimilarity.compute(a, b);
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
        assertThat(result).isLessThan(BigDecimal.ONE);
    }
}
