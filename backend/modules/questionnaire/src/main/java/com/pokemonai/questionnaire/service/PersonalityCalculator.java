package com.pokemonai.questionnaire.service;

import com.pokemonai.questionnaire.domain.TraitCategory;
import com.pokemonai.questionnaire.domain.UserAnswer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PersonalityCalculator {

    private static final BigDecimal MAX_VALUE = BigDecimal.valueOf(5);
    private static final int SCALE = 4;

    private PersonalityCalculator() {}

    /**
     * trait_score = Σ(answer_value_i * weight_i) / (5.0 * Σ(weight_i)) → [0.0, 1.0]
     */
    public static Map<TraitCategory, BigDecimal> calculate(List<UserAnswer> answers) {
        Map<TraitCategory, BigDecimal> weightedSum = new EnumMap<>(TraitCategory.class);
        Map<TraitCategory, BigDecimal> weightSum = new EnumMap<>(TraitCategory.class);

        for (UserAnswer answer : answers) {
            TraitCategory category = answer.getQuestion().getTraitCategory();
            BigDecimal weight = answer.getQuestion().getWeight();
            BigDecimal value = BigDecimal.valueOf(answer.getAnswerValue()).multiply(weight);

            weightedSum.merge(category, value, BigDecimal::add);
            weightSum.merge(category, weight, BigDecimal::add);
        }

        Map<TraitCategory, BigDecimal> scores = new EnumMap<>(TraitCategory.class);
        for (TraitCategory trait : TraitCategory.values()) {
            BigDecimal ws = weightedSum.getOrDefault(trait, BigDecimal.ZERO);
            BigDecimal wt = weightSum.getOrDefault(trait, BigDecimal.ZERO);

            BigDecimal score = wt.compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : ws.divide(MAX_VALUE.multiply(wt), SCALE, RoundingMode.HALF_UP);

            scores.put(trait, score);
        }
        return scores;
    }
}
