package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.Restaurant;
import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;

import java.util.ArrayList;
import java.util.List;

public class RestaurantService extends BaseService implements IRecommendationService {

    private final IAIService aiService;

    public RestaurantService(IAIService aiService) {
        this.aiService = aiService;
        logInfo("RestaurantService initialized");
    }

    @Override
    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        logInfo("Processing recommendation request");
        RecommendationResponse response = new RecommendationResponse();

        try {
            if (!aiService.isAvailable()) {
                throw new RuntimeException("AI Service is not available");
            }

            String aiOutput = aiService.getRecommendation(request);

            // Strip any filler text before the first numbered line
            aiOutput = aiOutput.replaceAll("(?s)^.*?(?=1\\.)", "").trim();
            response.setAiExplanation(aiOutput);

            List<Restaurant> restaurants = parseRestaurants(aiOutput);

            if (restaurants.isEmpty()) {
                logInfo("AI parsing failed, using fallback restaurants");
                restaurants = getMockRestaurants(request);
                response.setReasoning("AI output could not be parsed. Returning fallback restaurants.");
            } else {
                logInfo("Successfully parsed " + restaurants.size() + " restaurants");
                response.setReasoning("AI successfully generated recommendations based on your preferences.");
            }

            response.setRecommendations(restaurants);
            return response;

        } catch (Exception e) {
            logError("Error in getRecommendations", e);
            response.setRecommendations(getMockRestaurants(request));
            response.setReasoning("Service error occurred. Returning fallback restaurants.");
            return response;
        }
    }

    @Override
    public void resetService() {
        logInfo("Resetting restaurant service");
        aiService.resetConversation();
    }

    @Override
    public boolean isServiceHealthy() {
        return aiService != null && aiService.isAvailable();
    }

    @Override
    public String getServiceName() {
        return "Restaurant Recommendation Service";
    }

    private List<Restaurant> parseRestaurants(String aiOutput) {
        List<Restaurant> restaurants = new ArrayList<>();
        if (aiOutput == null || aiOutput.isEmpty()) return restaurants;

        String[] lines = aiOutput.split("\n");
        int idCounter = 1;

        for (String line : lines) {
            if (!line.matches("^\\d+\\..*")) continue;  // must start with number + dot

            String cleaned = line.replaceFirst("^\\d+\\.\\s*", "");
            String[] parts = cleaned.split("\\|");

            Restaurant r = new Restaurant();
            r.setId((long) idCounter++);
            r.setName(parts.length > 0 ? parts[0].trim() : "Unknown");
            r.setCuisine(parts.length > 1 ? parts[1].trim() : "Various");
            r.setLocation(parts.length > 2 ? parts[2].trim() : "Unknown");
            try {
                r.setRating(parts.length > 3 ? Double.parseDouble(parts[3].trim()) : 4.0);
            } catch (NumberFormatException e) {
                r.setRating(4.0);
            }
            r.setPriceRange(parts.length > 4 ? parts[4].trim() : "$$");
            r.setDescription(parts.length > 5 ? parts[5].trim() : "Recommended by AI.");

            restaurants.add(r);
        }
        return restaurants;
    }

    private List<Restaurant> getMockRestaurants(RecommendationRequest request) {
        List<Restaurant> restaurants = new ArrayList<>();

        Restaurant r1 = new Restaurant();
        r1.setId(1L);
        r1.setName("Sample Restaurant 1");
        r1.setCuisine(request.getCuisine());
        r1.setLocation(request.getLocation());
        r1.setRating(4.5);
        r1.setDescription("Fallback: Great restaurant with popular dishes.");
        r1.setPriceRange(request.getPriceRange());
        restaurants.add(r1);

        Restaurant r2 = new Restaurant();
        r2.setId(2L);
        r2.setName("Sample Restaurant 2");
        r2.setCuisine(request.getCuisine());
        r2.setLocation(request.getLocation());
        r2.setRating(4.3);
        r2.setDescription("Fallback: Another excellent choice for you.");
        r2.setPriceRange(request.getPriceRange());
        restaurants.add(r2);

        Restaurant r3 = new Restaurant();
        r3.setId(3L);
        r3.setName("Sample Restaurant 3");
        r3.setCuisine(request.getCuisine());
        r3.setLocation(request.getLocation());
        r3.setRating(4.2);
        r3.setDescription("Fallback: A popular spot for locals.");
        r3.setPriceRange(request.getPriceRange());
        restaurants.add(r3);

        return restaurants;
    }
}