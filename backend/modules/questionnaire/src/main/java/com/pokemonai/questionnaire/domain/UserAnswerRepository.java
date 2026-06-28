package com.pokemonai.questionnaire.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    @Query("SELECT a FROM UserAnswer a JOIN FETCH a.question WHERE a.userId = :userId")
    List<UserAnswer> findByUserId(@Param("userId") UUID userId);
}
