package com.restaurant.recommendation.core;

import com.restaurant.recommendation.controller.RecommendationController;
import com.restaurant.recommendation.service.OpenAIService;
import com.restaurant.recommendation.service.RestaurantService;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class RestaurantRecommendationServer {
    private HttpServer server;
    private final RecommendationController controller;

    public RestaurantRecommendationServer() {
        OpenAIService openAIService = new OpenAIService();
        RestaurantService restaurantService = new RestaurantService(openAIService);
        this.controller = new RecommendationController(restaurantService, openAIService);
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Define routes
        server.createContext("/api/recommendations", new RecommendationsHandler());
        server.createContext("/api/recommendations/health", new HealthHandler());
        server.createContext("/api/recommendations/test-openai", new TestOpenAIHandler());
        
        server.setExecutor(null); // Use default executor
        server.start();
        
        System.out.println("Restaurant Recommendation Server starting on port " + port);
        System.out.println("Server is running. Press Ctrl+C to stop.");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class RecommendationsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                controller.getRecommendations(exchange);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 200, "Restaurant Recommendation Service is running!");
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    private class TestOpenAIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String result = controller.testOpenAI();
                sendJsonResponse(exchange, 200, "{\"result\":\"" + escapeJson(result) + "\"}");
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/plain");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    public static void main(String[] args) {
        try {
            RestaurantRecommendationServer server = new RestaurantRecommendationServer();
            server.start(8080);
            
            // Keep the server running
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
            
            // Wait indefinitely
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
} 