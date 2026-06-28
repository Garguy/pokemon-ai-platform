package com.pokemonai.app;

import com.pokemonai.questionnaire.domain.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionRepositoryIT extends PostgresContainerBase {

    @Autowired
    QuestionRepository questionRepository;

    @Test
    void seedMigrationLoads18Questions() {
        assertThat(questionRepository.count()).isEqualTo(18);
    }

    @Test
    void questionsReturnedInDisplayOrder() {
        var questions = questionRepository.findAllByOrderByDisplayOrderAsc();
        assertThat(questions).hasSize(18);
        for (int i = 0; i < questions.size() - 1; i++) {
            assertThat(questions.get(i).getDisplayOrder())
                    .isLessThan(questions.get(i + 1).getDisplayOrder());
        }
    }

    @Test
    void eachTraitCategoryHasThreeQuestions() {
        var questions = questionRepository.findAll();
        var countByCategory = questions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        q -> q.getTraitCategory().name(),
                        java.util.stream.Collectors.counting()));

        assertThat(countByCategory).hasSize(6);
        countByCategory.values().forEach(count -> assertThat(count).isEqualTo(3));
    }
}
