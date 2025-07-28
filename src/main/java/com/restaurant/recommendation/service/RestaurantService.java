package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.Restaurant;
import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;
import java.util.List;
import java.util.ArrayList;

public class RestaurantService {

    private final OpenAIService openAIService;

    public RestaurantService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        RecommendationResponse response = new RecommendationResponse();
        
        // Get AI recommendation
        String aiRecommendation = openAIService.getRecommendation(request);
        response.setAiExplanation(aiRecommendation);
        
        // Here you can add database query logic
        // For now, return mock data
        List<Restaurant> restaurants = getMockRestaurants(request);
        response.setRecommendations(restaurants);
        
        return response;
    }

    private List<Restaurant> getMockRestaurants(RecommendationRequest request) {
        List<Restaurant> restaurants = new ArrayList<>();

        Restaurant restaurant1 = new Restaurant();
        restaurant1.setId(1L);
        restaurant1.setName("Sample Restaurant 1");
        restaurant1.setCuisine(request.getCuisine());
        restaurant1.setLocation(request.getLocation());
        restaurant1.setRating(4.5);
        restaurant1.setDescription("This is a great restaurant");
        restaurant1.setPriceRange(request.getPriceRange());
        restaurants.add(restaurant1);

        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Sample Restaurant 2");
        restaurant2.setCuisine(request.getCuisine());
        restaurant2.setLocation(request.getLocation());
        restaurant2.setRating(4.3);
        restaurant2.setDescription("Another excellent choice");
        restaurant2.setPriceRange(request.getPriceRange());
        restaurants.add(restaurant2);

        Restaurant restaurant3 = new Restaurant();
        restaurant3.setId(3L);
        restaurant3.setName("Sample Restaurant 3");
        restaurant3.setCuisine(request.getCuisine());
        restaurant3.setLocation(request.getLocation());
        restaurant3.setRating(4.2);
        restaurant3.setDescription("A popular spot for locals");
        restaurant3.setPriceRange(request.getPriceRange());
        restaurants.add(restaurant3);

        return restaurants;
    }
}
