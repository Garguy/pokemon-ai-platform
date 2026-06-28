package com.pokemonai.questionnaire.service;

import com.pokemonai.questionnaire.domain.*;
import com.pokemonai.shared.exception.PokemonAiException;
import com.pokemonai.shared.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuestionnaireService {

    private final QuestionRepository questionRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final PersonalityProfileRepository profileRepository;

    public QuestionnaireService(QuestionRepository questionRepository,
                                 UserAnswerRepository userAnswerRepository,
                                 PersonalityProfileRepository profileRepository) {
        this.questionRepository = questionRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.profileRepository = profileRepository;
    }

    public List<Question> findAllQuestions() {
        return questionRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Optional<PersonalityProfile> findProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId);
    }

    @Transactional
    public PersonalityProfile submitAnswers(UUID userId, List<AnswerInput> inputs) {
        if (inputs.isEmpty()) {
            throw new PokemonAiException("At least one answer is required");
        }

        List<UUID> questionIds = inputs.stream().map(AnswerInput::questionId).toList();

        Set<UUID> seen = new HashSet<>();
        for (UUID qid : questionIds) {
            if (!seen.add(qid)) {
                throw new PokemonAiException("Duplicate questionId in submission: " + qid);
            }
        }

        Map<UUID, Question> questionsById = questionRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        for (UUID qid : questionIds) {
            if (!questionsById.containsKey(qid)) {
                throw new ResourceNotFoundException("Question", qid);
            }
        }

        Map<UUID, UserAnswer> existingByQuestionId = userAnswerRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), Function.identity()));

        List<UserAnswer> allAnswers = new ArrayList<>(existingByQuestionId.values());

        for (AnswerInput input : inputs) {
            UserAnswer existing = existingByQuestionId.get(input.questionId());
            if (existing != null) {
                existing.updateValue(input.answerValue());
            } else {
                UserAnswer created = userAnswerRepository.save(
                        new UserAnswer(userId, questionsById.get(input.questionId()), input.answerValue()));
                allAnswers.add(created);
            }
        }

        Map<TraitCategory, BigDecimal> scores = PersonalityCalculator.calculate(allAnswers);

        try {
            return profileRepository.findByUserId(userId)
                    .map(profile -> {
                        updateProfileScores(profile, scores);
                        return profile;
                    })
                    .orElseGet(() -> {
                        PersonalityProfile profile = new PersonalityProfile(userId,
                                scores.get(TraitCategory.ENERGY),
                                scores.get(TraitCategory.CURIOSITY),
                                scores.get(TraitCategory.LEADERSHIP),
                                scores.get(TraitCategory.LOYALTY),
                                scores.get(TraitCategory.RISK),
                                scores.get(TraitCategory.CREATIVITY));
                        return profileRepository.save(profile);
                    });
        } catch (DataIntegrityViolationException e) {
            PersonalityProfile profile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> e);
            updateProfileScores(profile, scores);
            return profile;
        }
    }

    private void updateProfileScores(PersonalityProfile profile, Map<TraitCategory, BigDecimal> scores) {
        profile.update(
                scores.get(TraitCategory.ENERGY),
                scores.get(TraitCategory.CURIOSITY),
                scores.get(TraitCategory.LEADERSHIP),
                scores.get(TraitCategory.LOYALTY),
                scores.get(TraitCategory.RISK),
                scores.get(TraitCategory.CREATIVITY)
        );
    }

    public record AnswerInput(UUID questionId, int answerValue) {
        public AnswerInput {
            if (answerValue < 1 || answerValue > 5) {
                throw new PokemonAiException("answerValue must be between 1 and 5, got: " + answerValue);
            }
        }
    }
}
