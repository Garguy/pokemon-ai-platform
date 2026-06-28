package com.pokemonai.app.graphql;

import com.pokemonai.identity.service.UserService;
import com.pokemonai.questionnaire.domain.PersonalityProfile;
import com.pokemonai.questionnaire.domain.Question;
import com.pokemonai.questionnaire.service.QuestionnaireService;
import com.pokemonai.shared.exception.PokemonAiException;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final UserService userService;

    public QuestionnaireController(QuestionnaireService questionnaireService, UserService userService) {
        this.questionnaireService = questionnaireService;
        this.userService = userService;
    }

    @QueryMapping
    public List<Map<String, Object>> questions() {
        return questionnaireService.findAllQuestions().stream()
                .map(this::toQuestionMap)
                .toList();
    }

    @QueryMapping
    public Map<String, Object> myProfile(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UUID userId = resolveUserId(authentication);
        return questionnaireService.findProfileByUserId(userId)
                .map(this::toProfileMap)
                .orElse(null);
    }

    @MutationMapping
    public Map<String, Object> submitAnswers(
            @Argument List<Map<String, Object>> answers,
            Authentication authentication) {

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new PokemonAiException("Authentication required");
        }
        UUID userId = resolveUserId(authentication);

        List<QuestionnaireService.AnswerInput> inputs = answers.stream()
                .map(a -> {
                    String rawId = (String) a.get("questionId");
                    Number rawValue = (Number) a.get("answerValue");
                    if (rawId == null || rawValue == null) {
                        throw new PokemonAiException("questionId and answerValue are required");
                    }
                    try {
                        return new QuestionnaireService.AnswerInput(
                                UUID.fromString(rawId),
                                rawValue.intValue());
                    } catch (IllegalArgumentException e) {
                        throw new PokemonAiException("Invalid question ID format: " + rawId);
                    }
                })
                .toList();

        PersonalityProfile profile = questionnaireService.submitAnswers(userId, inputs);
        return toProfileMap(profile);
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new PokemonAiException("Authentication required");
        }
        String email = (String) authentication.getPrincipal();
        return userService.findByEmail(email).getId();
    }

    private Map<String, Object> toQuestionMap(Question q) {
        return Map.of(
                "id", q.getId().toString(),
                "text", q.getText(),
                "traitCategory", q.getTraitCategory().name(),
                "displayOrder", q.getDisplayOrder()
        );
    }

    private Map<String, Object> toProfileMap(PersonalityProfile p) {
        return Map.of(
                "id", p.getId().toString(),
                "energy", p.getEnergy().doubleValue(),
                "curiosity", p.getCuriosity().doubleValue(),
                "leadership", p.getLeadership().doubleValue(),
                "loyalty", p.getLoyalty().doubleValue(),
                "risk", p.getRisk().doubleValue(),
                "creativity", p.getCreativity().doubleValue(),
                "calculatedAt", p.getCalculatedAt().toString()
        );
    }
}
