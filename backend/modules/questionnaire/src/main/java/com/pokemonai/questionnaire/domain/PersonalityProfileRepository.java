package com.pokemonai.questionnaire.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonalityProfileRepository extends JpaRepository<PersonalityProfile, UUID> {

    Optional<PersonalityProfile> findByUserId(UUID userId);
}
