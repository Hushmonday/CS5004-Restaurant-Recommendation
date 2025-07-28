package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;
import com.restaurant.recommendation.model.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class RestaurantService {
    private final OpenAIService openAIService;

    public RestaurantService(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    public RecommendationResponse getRecommendations(RecommendationRequest request) {
        // Get AI recommendation
        String aiExplanation = openAIService.getRecommendation(request);
        
        // Create mock restaurants
        List<Restaurant> restaurants = getMockRestaurants(request);
        
        return new RecommendationResponse(restaurants, aiExplanation, "");
    }

    private List<Restaurant> getMockRestaurants(RecommendationRequest request) {
        List<Restaurant> restaurants = new ArrayList<>();
        
        Restaurant restaurant1 = new Restaurant();
        restaurant1.setId(1L);
        restaurant1.setName("Sample Restaurant 1");
        restaurant1.setCuisine("International");
        restaurant1.setLocation(request.getLocation() != null ? request.getLocation() : "Downtown");
        restaurant1.setRating(4.5);
        restaurant1.setDescription("This is a great restaurant");
        restaurant1.setPriceRange(request.getPriceRange() != null ? request.getPriceRange() : "$$");
        restaurants.add(restaurant1);
        
        Restaurant restaurant2 = new Restaurant();
        restaurant2.setId(2L);
        restaurant2.setName("Sample Restaurant 2");
        restaurant2.setCuisine("International");
        restaurant2.setLocation(request.getLocation() != null ? request.getLocation() : "Downtown");
        restaurant2.setRating(4.3);
        restaurant2.setDescription("Another excellent choice");
        restaurant2.setPriceRange(request.getPriceRange() != null ? request.getPriceRange() : "$$");
        restaurants.add(restaurant2);
        
        Restaurant restaurant3 = new Restaurant();
        restaurant3.setId(3L);
        restaurant3.setName("Sample Restaurant 3");
        restaurant3.setCuisine("International");
        restaurant3.setLocation(request.getLocation() != null ? request.getLocation() : "Downtown");
        restaurant3.setRating(4.2);
        restaurant3.setDescription("A popular spot for locals");
        restaurant3.setPriceRange(request.getPriceRange() != null ? request.getPriceRange() : "$$");
        restaurants.add(restaurant3);
        
        return restaurants;
    }
}
