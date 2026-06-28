package com.pokemonai.app.graphql;

import com.pokemonai.ai.service.RecommendationExplainer;
import com.pokemonai.identity.service.UserService;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.questionnaire.domain.PersonalityProfile;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.domain.RecommendationRepository;
import com.pokemonai.recommendation.service.RecommendationEngine;
import com.pokemonai.shared.exception.PokemonAiException;
import com.pokemonai.shared.exception.ResourceNotFoundException;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final RecommendationRepository recommendationRepository;
    private final PersonalityProfileRepository profileRepository;
    private final PokemonRepository pokemonRepository;
    private final UserService userService;
    private final RecommendationExplainer explainer;

    public RecommendationController(RecommendationEngine recommendationEngine,
                                     RecommendationRepository recommendationRepository,
                                     PersonalityProfileRepository profileRepository,
                                     PokemonRepository pokemonRepository,
                                     UserService userService,
                                     RecommendationExplainer explainer) {
        this.recommendationEngine = recommendationEngine;
        this.recommendationRepository = recommendationRepository;
        this.profileRepository = profileRepository;
        this.pokemonRepository = pokemonRepository;
        this.userService = userService;
        this.explainer = explainer;
    }

    @QueryMapping
    public List<Map<String, Object>> myRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        List<Recommendation> recommendations = recommendationEngine.findForUser(userId);
        List<UUID> pokemonIds = recommendations.stream().map(Recommendation::getPokemonId).toList();
        Map<UUID, Pokemon> pokemonById = pokemonRepository.findAllById(pokemonIds).stream()
                .collect(Collectors.toMap(Pokemon::getId, p -> p));
        return toMaps(recommendations, pokemonById);
    }

    @MutationMapping
    public List<Map<String, Object>> generateRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);

        PersonalityProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new PokemonAiException(
                        "Submit answers before generating recommendations"));

        var profileVector = new RecommendationEngine.ProfileVector(
                profile.getEnergy().doubleValue(),
                profile.getCuriosity().doubleValue(),
                profile.getLeadership().doubleValue(),
                profile.getLoyalty().doubleValue(),
                profile.getRisk().doubleValue(),
                profile.getCreativity().doubleValue());

        List<Recommendation> recommendations = recommendationEngine.generate(userId, profileVector);
        List<UUID> pokemonIds = recommendations.stream().map(Recommendation::getPokemonId).toList();
        Map<UUID, Pokemon> pokemonById = pokemonRepository.findAllById(pokemonIds).stream()
                .collect(Collectors.toMap(Pokemon::getId, p -> p));

        for (Recommendation rec : recommendations) {
            Pokemon pokemon = pokemonById.get(rec.getPokemonId());
            if (pokemon == null) {
                throw new ResourceNotFoundException("Pokemon", rec.getPokemonId());
            }
            String explanation = explainer.explain(
                    pokemon.getName(), pokemon.getDescription(),
                    profile.getEnergy().doubleValue(),
                    profile.getCuriosity().doubleValue(),
                    profile.getLeadership().doubleValue(),
                    profile.getLoyalty().doubleValue(),
                    profile.getRisk().doubleValue(),
                    profile.getCreativity().doubleValue());
            rec.setExplanation(explanation);
            recommendationRepository.save(rec);
        }

        return toMaps(recommendations, pokemonById);
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new PokemonAiException("Authentication required");
        }
        String email = (String) authentication.getPrincipal();
        return userService.findByEmail(email).getId();
    }

    private List<Map<String, Object>> toMaps(List<Recommendation> recommendations, Map<UUID, Pokemon> pokemonById) {
        return recommendations.stream()
                .map(r -> {
                    Pokemon pokemon = pokemonById.get(r.getPokemonId());
                    if (pokemon == null) {
                        throw new ResourceNotFoundException("Pokemon", r.getPokemonId());
                    }
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", r.getId().toString());
                    map.put("pokemon", Map.of(
                            "id", pokemon.getId().toString(),
                            "externalId", pokemon.getExternalId(),
                            "name", pokemon.getName(),
                            "description", pokemon.getDescription() != null ? pokemon.getDescription() : "",
                            "imageUrl", pokemon.getImageUrl() != null ? pokemon.getImageUrl() : "",
                            "syncedAt", pokemon.getSyncedAt().toString()
                    ));
                    map.put("matchScore", r.getMatchScore().doubleValue());
                    map.put("rank", (int) r.getRank());
                    map.put("explanation", r.getExplanation());
                    map.put("generatedAt", r.getGeneratedAt().toString());
                    return map;
                })
                .toList();
    }
}
