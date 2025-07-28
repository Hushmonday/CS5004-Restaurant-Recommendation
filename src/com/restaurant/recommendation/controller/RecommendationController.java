package com.restaurant.recommendation.controller;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;
import com.restaurant.recommendation.service.OpenAIService;
import com.restaurant.recommendation.service.RestaurantService;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RecommendationController {
    private final RestaurantService restaurantService;
    private final OpenAIService openAIService;

    public RecommendationController(RestaurantService restaurantService, OpenAIService openAIService) {
        this.restaurantService = restaurantService;
        this.openAIService = openAIService;
    }

    public void getRecommendations(HttpExchange exchange) throws IOException {
        try {
            // Read request body
            String requestBody = readRequestBody(exchange);
            
            // Parse the request
            RecommendationRequest request = parseRecommendationRequest(requestBody);
            
            // Get recommendations
            RecommendationResponse response = restaurantService.getRecommendations(request);
            
            // Send response
            sendJsonResponse(exchange, 200, convertToJson(response));
            
        } catch (Exception e) {
            System.err.println("Error handling recommendation request: " + e.getMessage());
            sendJsonResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    public String testOpenAI() {
        try {
            RecommendationRequest testRequest = new RecommendationRequest();
            testRequest.setUserPreference("test");
            testRequest.setLocation("test");
            testRequest.setPriceRange("test");
            
            return openAIService.getRecommendation(testRequest);
        } catch (Exception e) {
            return "Error testing OpenAI: " + e.getMessage();
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private RecommendationRequest parseRecommendationRequest(String json) {
        RecommendationRequest request = new RecommendationRequest();
        
        // Simple JSON parsing
        request.setUserPreference(extractValue(json, "userPreference"));
        request.setLocation(extractValue(json, "location"));
        request.setPriceRange(extractValue(json, "priceRange"));
        
        return request;
    }

    private String extractValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int start = json.indexOf(searchKey);
        if (start != -1) {
            start += searchKey.length();
            int end = json.indexOf("\"", start);
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return null;
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (java.io.OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String convertToJson(RecommendationResponse response) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"recommendations\":[");
        
        if (response.getRecommendations() != null) {
            for (int i = 0; i < response.getRecommendations().size(); i++) {
                if (i > 0) json.append(",");
                json.append(convertRestaurantToJson(response.getRecommendations().get(i)));
            }
        }
        
        json.append("],");
        json.append("\"aiExplanation\":\"").append(escapeJson(response.getAiExplanation() != null ? response.getAiExplanation() : "")).append("\",");
        json.append("\"reasoning\":\"").append(escapeJson(response.getReasoning() != null ? response.getReasoning() : "")).append("\"");
        json.append("}");
        return json.toString();
    }
    
    private String convertRestaurantToJson(com.restaurant.recommendation.model.Restaurant restaurant) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(restaurant.getId()).append(",");
        json.append("\"name\":\"").append(escapeJson(restaurant.getName() != null ? restaurant.getName() : "")).append("\",");
        json.append("\"cuisine\":\"").append(escapeJson(restaurant.getCuisine() != null ? restaurant.getCuisine() : "")).append("\",");
        json.append("\"location\":\"").append(escapeJson(restaurant.getLocation() != null ? restaurant.getLocation() : "")).append("\",");
        json.append("\"rating\":").append(restaurant.getRating() != null ? restaurant.getRating() : 0).append(",");
        json.append("\"description\":\"").append(escapeJson(restaurant.getDescription() != null ? restaurant.getDescription() : "")).append("\",");
        json.append("\"priceRange\":\"").append(escapeJson(restaurant.getPriceRange() != null ? restaurant.getPriceRange() : "")).append("\"");
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
