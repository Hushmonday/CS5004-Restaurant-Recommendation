package com.restaurant.recommendation.controller;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;
import com.restaurant.recommendation.service.RestaurantService;
import com.restaurant.recommendation.service.OpenAIService;
import com.restaurant.recommendation.core.RestaurantRecommendationServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class RecommendationController {
    private final RestaurantService restaurantService;
    private final OpenAIService openAIService;

    public RecommendationController(RestaurantService restaurantService, OpenAIService openAIService) {
        this.restaurantService = restaurantService;
        this.openAIService = openAIService;
    }

    public void handleGetRecommendations(HttpExchange exchange) throws IOException {
        try {
            // Read request body
            InputStream requestBody = exchange.getRequestBody();
            Scanner scanner = new Scanner(requestBody).useDelimiter("\\A");
            String requestBodyString = scanner.hasNext() ? scanner.next() : "";
            
            // Parse JSON request manually
            RecommendationRequest request = parseRecommendationRequest(requestBodyString);
            
            // Process request
            RecommendationResponse response = restaurantService.getRecommendations(request);
            
            // Send response
            RestaurantRecommendationServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            RestaurantRecommendationServer.sendResponse(exchange, 500, "Error processing request: " + e.getMessage());
        }
    }

    private RecommendationRequest parseRecommendationRequest(String jsonString) {
        RecommendationRequest request = new RecommendationRequest();
        
        // Simple JSON parsing - extract values between quotes
        String[] lines = jsonString.split(",");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("\"userPreference\"")) {
                request.setUserPreference(extractValue(line));
            } else if (line.contains("\"location\"")) {
                request.setLocation(extractValue(line));
            } else if (line.contains("\"cuisine\"")) {
                request.setCuisine(extractValue(line));
            } else if (line.contains("\"priceRange\"")) {
                request.setPriceRange(extractValue(line));
            } else if (line.contains("\"numberOfPeople\"")) {
                String value = extractValue(line);
                if (!value.isEmpty()) {
                    request.setNumberOfPeople(Integer.parseInt(value));
                }
            } else if (line.contains("\"occasion\"")) {
                request.setOccasion(extractValue(line));
            }
        }
        
        return request;
    }
    
    private String extractValue(String line) {
        int start = line.indexOf("\"", line.indexOf("\"") + 1) + 1;
        int end = line.indexOf("\"", start);
        if (start > 0 && end > start) {
            return line.substring(start, end);
        }
        return "";
    }

    public void handleHealth(HttpExchange exchange) throws IOException {
        String response = "Restaurant Recommendation Service is running!";
        RestaurantRecommendationServer.sendResponse(exchange, 200, response);
    }
    
    public void handleTestOpenAI(HttpExchange exchange) throws IOException {
        try {
            // Create a simple test request
            RecommendationRequest testRequest = new RecommendationRequest();
            testRequest.setUserPreference("Likes Chinese cuisine");
            testRequest.setLocation("Beijing");
            testRequest.setCuisine("Sichuan");
            testRequest.setPriceRange("Medium");
            testRequest.setNumberOfPeople(2);
            testRequest.setOccasion("Friends gathering");
            
            String result = openAIService.getRecommendation(testRequest);
            String response = "OpenAI API test successful!\n\nRecommendation result:\n" + result;
            RestaurantRecommendationServer.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            String response = "OpenAI API test failed:\n" + e.getMessage();
            RestaurantRecommendationServer.sendResponse(exchange, 500, response);
        }
    }
}
