package com.restaurant.recommendation.core;

import com.restaurant.recommendation.controller.RecommendationController;
import com.restaurant.recommendation.service.*;
import com.restaurant.recommendation.model.RecommendationResponse;
import com.restaurant.recommendation.model.Restaurant;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.List;

public class RestaurantRecommendationServer {
    private static final int PORT = 8080;

    // 使用接口类型 - 简单的依赖注入
    private static final IAIService aiService = new OpenAIService();
    private static final IRecommendationService restaurantService = new RestaurantService(aiService);
    private static final RecommendationController recommendationController =
        new RecommendationController(restaurantService, aiService);

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Set up routes
        server.createContext("/api/recommendations", new RecommendationsHandler());
        server.createContext("/api/recommendations/health", new HealthHandler());
        server.createContext("/api/recommendations/test-openai", new TestOpenAIHandler());
        server.createContext("/api/recommendations/reset", new ResetHandler());

        // Set thread pool
        server.setExecutor(Executors.newFixedThreadPool(10));

        System.out.println("Restaurant Recommendation Server starting on port " + PORT);
        server.start();
        System.out.println("Server is running. Press Ctrl+C to stop.");
    }

    static class RecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                recommendationController.handleGetRecommendations(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                recommendationController.handleHealth(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    static class TestOpenAIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                recommendationController.handleTestOpenAI(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                recommendationController.handleReset(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    public static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = convertToJson(response);
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private static String convertToJson(Object obj) {
        if (obj instanceof RecommendationResponse) {
            return convertRecommendationResponseToJson((RecommendationResponse) obj);
        }
        return "{}";
    }

    private static String convertRecommendationResponseToJson(RecommendationResponse response) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"recommendations\":[");

        List<Restaurant> restaurants = response.getRecommendations();
        if (restaurants != null) {
            for (int i = 0; i < restaurants.size(); i++) {
                if (i > 0) json.append(",");
                json.append(convertRestaurantToJson(restaurants.get(i)));
            }
        }

        json.append("],");
        json.append("\"aiExplanation\":\"").append(response.getAiExplanation() != null ? response.getAiExplanation() : "").append("\",");
        json.append("\"reasoning\":\"").append(response.getReasoning() != null ? response.getReasoning() : "").append("\"");
        json.append("}");
        return json.toString();
    }

    private static String convertRestaurantToJson(Restaurant restaurant) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(restaurant.getId()).append(",");
        json.append("\"name\":\"").append(restaurant.getName() != null ? restaurant.getName() : "").append("\",");
        json.append("\"cuisine\":\"").append(restaurant.getCuisine() != null ? restaurant.getCuisine() : "").append("\",");
        json.append("\"location\":\"").append(restaurant.getLocation() != null ? restaurant.getLocation() : "").append("\",");
        json.append("\"rating\":").append(restaurant.getRating() != null ? restaurant.getRating() : 0).append(",");
        json.append("\"description\":\"").append(restaurant.getDescription() != null ? restaurant.getDescription() : "").append("\",");
        json.append("\"priceRange\":\"").append(restaurant.getPriceRange() != null ? restaurant.getPriceRange() : "").append("\"");
        json.append("}");
        return json.toString();
    }
}