package com.restaurant.recommendation.controller;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;
import com.restaurant.recommendation.service.IRecommendationService;
import com.restaurant.recommendation.service.IAIService;
import com.restaurant.recommendation.core.RestaurantRecommendationServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class RecommendationController {
    private final IRecommendationService restaurantService;
    private final IAIService aiService;

    public RecommendationController(IRecommendationService restaurantService, IAIService aiService) {
        this.restaurantService = restaurantService;
        this.aiService = aiService;
        System.out.println("RecommendationController initialized");
    }

    public void handleGetRecommendations(HttpExchange exchange) throws IOException {
        try {
            // Read request body
            InputStream requestBody = exchange.getRequestBody();
            Scanner scanner = new Scanner(requestBody).useDelimiter("\\A");
            String requestBodyString = scanner.hasNext() ? scanner.next() : "";

            // Debug: Print received JSON
            System.out.println("=== Received Request Body ===");
            System.out.println(requestBodyString);
            System.out.println("============================");

            // Parse JSON request manually
            RecommendationRequest request = parseRecommendationRequest(requestBodyString);

            // Debug: Print parsed request
            System.out.println("=== Parsed Request ===");
            System.out.println("UserPreference: " + request.getUserPreference());
            System.out.println("Location: " + request.getLocation());
            System.out.println("Cuisine: " + request.getCuisine());
            System.out.println("PriceRange: " + request.getPriceRange());
            System.out.println("=====================");

            // Process request
            RecommendationResponse response = restaurantService.getRecommendations(request);

            // Send response
            RestaurantRecommendationServer.sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            System.err.println("Error in handleGetRecommendations: " + e.getMessage());
            e.printStackTrace();
            RestaurantRecommendationServer.sendResponse(exchange, 500, "Error processing request: " + e.getMessage());
        }
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

            String result = aiService.getRecommendation(testRequest);
            String response = "OpenAI API test successful!\n\nRecommendation result:\n" + result;
            RestaurantRecommendationServer.sendResponse(exchange, 200, response);
        } catch (Exception e) {
            String response = "OpenAI API test failed:\n" + e.getMessage();
            RestaurantRecommendationServer.sendResponse(exchange, 500, response);
        }
    }

    public void handleReset(HttpExchange exchange) throws IOException {
        try {
            // Reset conversation history in OpenAI service
            aiService.resetConversation();

            String response = "Conversation history has been reset, location information cleared.";
            RestaurantRecommendationServer.sendResponse(exchange, 200, response);

            System.out.println("Conversation reset successful");
        } catch (Exception e) {
            System.err.println("Error resetting conversation: " + e.getMessage());
            e.printStackTrace();
            RestaurantRecommendationServer.sendResponse(exchange, 500,
                "Reset failed: " + e.getMessage());
        }
    }

    private RecommendationRequest parseRecommendationRequest(String jsonString) {
        RecommendationRequest request = new RecommendationRequest();

        if (jsonString == null || jsonString.trim().isEmpty()) {
            System.err.println("Warning: Empty JSON received");
            return request;
        }

        try {
            jsonString = jsonString.trim();
            if (jsonString.startsWith("{")) {
                jsonString = jsonString.substring(1);
            }
            if (jsonString.endsWith("}")) {
                jsonString = jsonString.substring(0, jsonString.length() - 1);
            }

            String[] pairs = splitJsonPairs(jsonString);

            for (String pair : pairs) {
                pair = pair.trim();
                if (pair.isEmpty()) continue;

                int colonIndex = pair.indexOf(":");
                if (colonIndex == -1) continue;

                String key = pair.substring(0, colonIndex).trim();
                String value = pair.substring(colonIndex + 1).trim();

                key = removeQuotes(key);

                switch (key) {
                    case "userPreference":
                        request.setUserPreference(removeQuotes(value));
                        break;
                    case "location":
                        request.setLocation(removeQuotes(value));
                        break;
                    case "cuisine":
                        request.setCuisine(removeQuotes(value));
                        break;
                    case "priceRange":
                        request.setPriceRange(removeQuotes(value));
                        break;
                    case "numberOfPeople":
                        try {
                            request.setNumberOfPeople(Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing numberOfPeople: " + value);
                        }
                        break;
                    case "occasion":
                        request.setOccasion(removeQuotes(value));
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            e.printStackTrace();
        }

        return request;
    }

    private String[] splitJsonPairs(String jsonString) {
        java.util.List<String> pairs = new java.util.ArrayList<>();
        StringBuilder currentPair = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = '"';

        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);

            if ((c == '"' || c == '\'') && (i == 0 || jsonString.charAt(i-1) != '\\')) {
                if (!inQuotes) {
                    inQuotes = true;
                    quoteChar = c;
                } else if (c == quoteChar) {
                    inQuotes = false;
                }
            }

            if (c == ',' && !inQuotes) {
                pairs.add(currentPair.toString());
                currentPair = new StringBuilder();
            } else {
                currentPair.append(c);
            }
        }

        if (currentPair.length() > 0) {
            pairs.add(currentPair.toString());
        }

        return pairs.toArray(new String[0]);
    }

    private String removeQuotes(String str) {
        if (str == null) return "";

        str = str.trim();
        if ((str.startsWith("\"") && str.endsWith("\"")) ||
            (str.startsWith("'") && str.endsWith("'"))) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}
