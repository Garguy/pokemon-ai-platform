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

class PersonalityCalculatorTest {

    @Test
    void allAnswersFiveProducesScoreOne() {
        List<UserAnswer> answers = allAnswersWithValue(5);

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        for (TraitCategory trait : TraitCategory.values()) {
            assertThat(scores.get(trait))
                    .as("score for %s", trait)
                    .isEqualByComparingTo(BigDecimal.ONE);
        }
    }

    @Test
    void allAnswersOneProducesScorePointTwo() {
        List<UserAnswer> answers = allAnswersWithValue(1);

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        for (TraitCategory trait : TraitCategory.values()) {
            assertThat(scores.get(trait))
                    .as("score for %s", trait)
                    .isEqualByComparingTo(new BigDecimal("0.2000"));
        }
    }

    @Test
    void emptyAnswersProducesZeroForAllTraits() {
        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(List.of());

        for (TraitCategory trait : TraitCategory.values()) {
            assertThat(scores.get(trait)).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Test
    void onlyOneTraitAnsweredLeavesOthersZero() {
        List<UserAnswer> answers = List.of(
                makeAnswer(TraitCategory.ENERGY, new BigDecimal("1.0"), 3)
        );

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(answers);

        assertThat(scores.get(TraitCategory.ENERGY)).isEqualByComparingTo(new BigDecimal("0.6000"));
        assertThat(scores.get(TraitCategory.CURIOSITY)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    // --- helpers ---

    private List<UserAnswer> allAnswersWithValue(int value) {
        return List.of(
                makeAnswer(TraitCategory.ENERGY,     new BigDecimal("1.0"), value),
                makeAnswer(TraitCategory.CURIOSITY,  new BigDecimal("1.0"), value),
                makeAnswer(TraitCategory.LEADERSHIP, new BigDecimal("1.0"), value),
                makeAnswer(TraitCategory.LOYALTY,    new BigDecimal("1.0"), value),
                makeAnswer(TraitCategory.RISK,       new BigDecimal("1.0"), value),
                makeAnswer(TraitCategory.CREATIVITY, new BigDecimal("1.0"), value)
        );
    }

    private UserAnswer makeAnswer(TraitCategory category, BigDecimal weight, int value) {
        Question question = buildQuestion(category, weight);
        return new UserAnswer(UUID.randomUUID(), question, value);
    }

    private Question buildQuestion(TraitCategory category, BigDecimal weight) {
        return new Question(category, weight);
    }
}
