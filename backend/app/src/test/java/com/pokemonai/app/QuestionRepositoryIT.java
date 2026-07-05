package com.pokemonai.app;

import com.pokemonai.questionnaire.domain.QuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class QuestionRepositoryIT extends PostgresContainerBase {

    @Autowired
    QuestionRepository questionRepository;

    @Test
    void seedMigrationLoads20Questions() {
        assertThat(questionRepository.count()).isEqualTo(20);
    }

    @Test
    void questionsReturnedInDisplayOrder() {
        var questions = questionRepository.findAllByOrderByDisplayOrderAsc();
        assertThat(questions).hasSize(20);
        for (int i = 0; i < questions.size() - 1; i++) {
            assertThat(questions.get(i).getDisplayOrder())
                    .isLessThan(questions.get(i + 1).getDisplayOrder());
        }
    }

    @Test
    void allSixTraitCategoriesAreRepresented() {
        var questions = questionRepository.findAll();
        Map<String, Long> countByCategory = questions.stream()
                .collect(Collectors.groupingBy(
                        q -> q.getTraitCategory().name(),
                        Collectors.counting()));

        assertThat(countByCategory).hasSize(6);
        // ENERGY=4, CURIOSITY=3, LEADERSHIP=3, LOYALTY=3, RISK=4, CREATIVITY=3
        assertThat(countByCategory.get("ENERGY")).isEqualTo(4L);
        assertThat(countByCategory.get("CURIOSITY")).isEqualTo(3L);
        assertThat(countByCategory.get("LEADERSHIP")).isEqualTo(3L);
        assertThat(countByCategory.get("LOYALTY")).isEqualTo(3L);
        assertThat(countByCategory.get("RISK")).isEqualTo(4L);
        assertThat(countByCategory.get("CREATIVITY")).isEqualTo(3L);
    }
}
