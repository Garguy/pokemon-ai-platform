package com.pokemonai.app.graphql;

import com.pokemonai.ai.service.AiRecommendationService;
import com.pokemonai.ai.service.AiRecommendationService.RankedMatch;
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

import java.math.BigDecimal;
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

    public RecommendationController(RecommendationEngine recommendationEngine,
                                     PersonalityProfileRepository profileRepository,
                                     PokemonRepository pokemonRepository,
                                     UserService userService,
                                     AiRecommendationService aiRecommendationService) {
        this.recommendationEngine = recommendationEngine;
        this.profileRepository = profileRepository;
        this.pokemonRepository = pokemonRepository;
        this.userService = userService;
        this.aiRecommendationService = aiRecommendationService;
    }

    @QueryMapping
    public List<Map<String, Object>> myRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);
        return toMaps(recommendationEngine.findForUser(userId));
    }

    @MutationMapping
    public List<Map<String, Object>> generateRecommendations(Authentication authentication) {
        UUID userId = resolveUserId(authentication);

        PersonalityProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new PokemonAiException("Complete the questionnaire before generating matches"));

        // Single Gemini call — returns names + scores + explanations together
        List<RankedMatch> matches = aiRecommendationService.rankPokemon(
                profile.getEnergy().doubleValue(),
                profile.getCuriosity().doubleValue(),
                profile.getLeadership().doubleValue(),
                profile.getLoyalty().doubleValue(),
                profile.getRisk().doubleValue(),
                profile.getCreativity().doubleValue());

        List<RankedPokemon> ranked = new ArrayList<>();
        for (RankedMatch match : matches) {
            pokemonRepository.findByNameIgnoreCase(match.name()).ifPresent(pokemon ->
                ranked.add(new RankedPokemon(pokemon.getId(), BigDecimal.valueOf(match.score()), match.explanation()))
            );
        }

        if (ranked.isEmpty()) {
            throw new PokemonAiException("Could not find any matching Pokémon — please try again");
        }

        return toMaps(recommendationEngine.save(userId, ranked));
    }

    private UUID resolveUserId(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            throw new PokemonAiException("Authentication required");
        }
        return userService.findByEmail((String) authentication.getPrincipal()).getId();
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
