package com.pokemonai.app.graphql;

import com.pokemonai.ai.service.AiRecommendationService;
import com.pokemonai.ai.service.RecommendationExplainer;
import com.pokemonai.identity.service.UserService;
import com.pokemonai.pokemon.domain.Pokemon;
import com.pokemonai.pokemon.domain.PokemonRepository;
import com.pokemonai.questionnaire.domain.PersonalityProfile;
import com.pokemonai.questionnaire.domain.PersonalityProfileRepository;
import com.pokemonai.recommendation.domain.Recommendation;
import com.pokemonai.recommendation.service.RecommendationEngine;
import com.pokemonai.recommendation.service.RecommendationEngine.RankedPokemon;
import com.pokemonai.shared.exception.PokemonAiException;
import com.pokemonai.shared.exception.ResourceNotFoundException;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class RecommendationController {

    private final RecommendationEngine recommendationEngine;
    private final PersonalityProfileRepository profileRepository;
    private final PokemonRepository pokemonRepository;
    private final UserService userService;
    private final AiRecommendationService aiRecommendationService;
    private final RecommendationExplainer explainer;

    public RecommendationController(RecommendationEngine recommendationEngine,
                                     PersonalityProfileRepository profileRepository,
                                     PokemonRepository pokemonRepository,
                                     UserService userService,
                                     AiRecommendationService aiRecommendationService,
                                     RecommendationExplainer explainer) {
        this.recommendationEngine = recommendationEngine;
        this.profileRepository = profileRepository;
        this.pokemonRepository = pokemonRepository;
        this.userService = userService;
        this.aiRecommendationService = aiRecommendationService;
        this.explainer = explainer;
    }

    @QueryMapping
    public List<Map<String, Object>> myRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        List<Recommendation> recommendations = recommendationEngine.findForUser(userId);
        return toMaps(recommendations);
    }

    @MutationMapping
    public List<Map<String, Object>> generateRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);

        PersonalityProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new PokemonAiException("Submit answers before generating recommendations"));

        // Ask AI for ranked Pokémon names based on personality
        List<String> pokemonNames = aiRecommendationService.rankPokemon(
                profile.getEnergy().doubleValue(),
                profile.getCuriosity().doubleValue(),
                profile.getLeadership().doubleValue(),
                profile.getLoyalty().doubleValue(),
                profile.getRisk().doubleValue(),
                profile.getCreativity().doubleValue());

        // Look up each Pokémon in the DB and generate an explanation
        List<RankedPokemon> ranked = new ArrayList<>();
        for (String name : pokemonNames) {
            Pokemon pokemon = pokemonRepository.findByNameIgnoreCase(name)
                    .orElse(null);
            if (pokemon == null) continue;

            String explanation = explainer.explain(
                    pokemon.getName(), pokemon.getDescription(),
                    profile.getEnergy().doubleValue(),
                    profile.getCuriosity().doubleValue(),
                    profile.getLeadership().doubleValue(),
                    profile.getLoyalty().doubleValue(),
                    profile.getRisk().doubleValue(),
                    profile.getCreativity().doubleValue());

            ranked.add(new RankedPokemon(pokemon.getId(), explanation));
        }

        if (ranked.isEmpty()) {
            throw new PokemonAiException("Could not find any matching Pokémon — please try again");
        }

        List<Recommendation> recommendations = recommendationEngine.save(userId, ranked);
        return toMaps(recommendations);
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new PokemonAiException("Authentication required");
        }
        String email = (String) authentication.getPrincipal();
        return userService.findByEmail(email).getId();
    }

    private List<Map<String, Object>> toMaps(List<Recommendation> recommendations) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Recommendation r : recommendations) {
            Pokemon pokemon = pokemonRepository.findById(r.getPokemonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pokemon", r.getPokemonId()));
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
            result.add(map);
        }
        return result;
    }
}
