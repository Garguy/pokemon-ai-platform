package com.pokemonai.app;

import com.pokemonai.identity.domain.User;
import com.pokemonai.identity.domain.UserRepository;
import com.pokemonai.questionnaire.domain.*;
import com.pokemonai.questionnaire.service.QuestionnaireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionnaireServiceIT extends PostgresContainerBase {

    @Autowired QuestionnaireService questionnaireService;
    @Autowired QuestionRepository questionRepository;
    @Autowired UserAnswerRepository userAnswerRepository;
    @Autowired PersonalityProfileRepository profileRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private UUID userId;
    private List<Question> questions;

    @BeforeEach
    void setUp() {
        userAnswerRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(new User("trainer@pokemon.com", passwordEncoder.encode("pass")));
        userId = user.getId();
        questions = questionRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void submitAnswersPersistsUserAnswersAndProfile() {
        List<QuestionnaireService.AnswerInput> inputs = questions.stream()
                .map(q -> new QuestionnaireService.AnswerInput(q.getId(), 3))
                .toList();

        PersonalityProfile profile = questionnaireService.submitAnswers(userId, inputs);

        assertThat(userAnswerRepository.findByUserId(userId)).hasSize(18);
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getUserId()).isEqualTo(userId);
        // weights per trait: 1.0+1.2+0.8=3.0; score = (3*3.0)/(5*3.0) = 0.6
        assertThat(profile.getEnergy()).isEqualByComparingTo("0.6000");
        assertThat(profile.getCuriosity()).isEqualByComparingTo("0.6000");
    }

    @Test
    void resubmissionUpsertsBothAnswersAndProfile() {
        List<QuestionnaireService.AnswerInput> firstInputs = questions.stream()
                .map(q -> new QuestionnaireService.AnswerInput(q.getId(), 2))
                .toList();
        questionnaireService.submitAnswers(userId, firstInputs);

        List<QuestionnaireService.AnswerInput> secondInputs = questions.stream()
                .map(q -> new QuestionnaireService.AnswerInput(q.getId(), 5))
                .toList();
        PersonalityProfile updated = questionnaireService.submitAnswers(userId, secondInputs);

        // Only one profile row
        assertThat(profileRepository.findByUserId(userId)).isPresent();
        assertThat(profileRepository.count()).isEqualTo(1);
        // Scores should now reflect value=5
        assertThat(updated.getEnergy()).isEqualByComparingTo("1.0000");
        // Only 18 answer rows — not 36
        assertThat(userAnswerRepository.findByUserId(userId)).hasSize(18);
    }
}
