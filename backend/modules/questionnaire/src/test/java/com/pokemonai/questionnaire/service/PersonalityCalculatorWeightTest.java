package com.pokemonai.questionnaire.service;

import com.pokemonai.questionnaire.domain.Question;
import com.pokemonai.questionnaire.domain.TraitCategory;
import com.pokemonai.questionnaire.domain.UserAnswer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersonalityCalculatorWeightTest {

    // Two ENERGY answers: weight=1.0 value=4, weight=2.0 value=2
    // weightedSum = 4*1.0 + 2*2.0 = 8.0
    // weightSum   = 1.0 + 2.0    = 3.0
    // score = 8.0 / (5.0 * 3.0) = 8/15 = 0.5333
    @Test
    void weightedAverageIsCorrect() {
        List<UserAnswer> answers = List.of(
                answer(TraitCategory.ENERGY, new BigDecimal("1.0"), 4),
                answer(TraitCategory.ENERGY, new BigDecimal("2.0"), 2)
        );

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        assertThat(scores.get(TraitCategory.ENERGY))
                .isEqualByComparingTo(new BigDecimal("0.5333"));
    }

    // Single question weight=1.2 value=5: 5*1.2 / (5*1.2) = 1.0
    @Test
    void singleHighWeightAnswerFiveStillCapsAtOne() {
        List<UserAnswer> answers = List.of(
                answer(TraitCategory.RISK, new BigDecimal("1.2"), 5)
        );

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        assertThat(scores.get(TraitCategory.RISK))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    // Single question weight=0.8 value=1: 1*0.8 / (5*0.8) = 0.2
    @Test
    void singleLowWeightAnswerOneGivesPointTwo() {
        List<UserAnswer> answers = List.of(
                answer(TraitCategory.LOYALTY, new BigDecimal("0.8"), 1)
        );

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        assertThat(scores.get(TraitCategory.LOYALTY))
                .isEqualByComparingTo(new BigDecimal("0.2000"));
    }

    // All three seeded weights (1.0, 1.2, 0.8) all value=3
    // weightedSum = 3*1.0 + 3*1.2 + 3*0.8 = 9.0
    // weightSum   = 1.0 + 1.2 + 0.8 = 3.0
    // score = 9.0 / (5.0 * 3.0) = 0.6
    @Test
    void mixedWeightsAllValueThreeGivesPointSix() {
        List<UserAnswer> answers = List.of(
                answer(TraitCategory.CREATIVITY, new BigDecimal("1.0"), 3),
                answer(TraitCategory.CREATIVITY, new BigDecimal("1.2"), 3),
                answer(TraitCategory.CREATIVITY, new BigDecimal("0.8"), 3)
        );

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        assertThat(scores.get(TraitCategory.CREATIVITY))
                .isEqualByComparingTo(new BigDecimal("0.6000"));
    }

    private UserAnswer answer(TraitCategory category, BigDecimal weight, int value) {
        return new UserAnswer(UUID.randomUUID(), new Question(category, weight), value);
    }
}
