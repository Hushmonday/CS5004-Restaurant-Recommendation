package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class OpenAIService {
    private String azureApiKey;
    private String azureEndpoint;
    private String azureDeployment;
    private HttpClient httpClient;

    public OpenAIService() {
        loadProperties();
        httpClient = HttpClient.newHttpClient();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                azureApiKey = props.getProperty("azure.openai.api.key");
                azureEndpoint = props.getProperty("azure.openai.endpoint");
                azureDeployment = props.getProperty("azure.openai.deployment");
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
        }
    }

    public String getRecommendation(RecommendationRequest request) {
        try {
            if (azureApiKey == null || azureEndpoint == null || azureDeployment == null) {
                return "Error: Azure OpenAI configuration not found. Please check application.properties.";
            }

            String prompt = buildPrompt(request);
            String jsonPayload = buildJsonPayload(prompt);

            String url = azureEndpoint + "openai/deployments/" + azureDeployment + "/chat/completions?api-version=2024-12-01-preview";
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("api-key", azureApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseAzureResponse(response.body());
            } else {
                return "Error: Azure OpenAI API call failed. Status: " + response.statusCode() + " Response: " + response.body();
            }

        } catch (Exception e) {
            System.err.println("Error getting recommendation from Azure OpenAI: " + e.getMessage());
            return "Sorry, we are temporarily unable to recommend restaurants. Please try again later. Error: " + e.getMessage();
        }
    }

    private String buildJsonPayload(String prompt) {
        return "{\n" +
               "  \"messages\": [\n" +
               "    {\n" +
               "      \"role\": \"system\",\n" +
               "      \"content\": \"You are a helpful restaurant recommendation assistant. Provide friendly and helpful restaurant suggestions.\"\n" +
               "    },\n" +
               "    {\n" +
               "      \"role\": \"user\",\n" +
               "      \"content\": \"" + escapeJson(prompt) + "\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"temperature\": 0.7,\n" +
               "  \"top_p\": 1.0,\n" +
               "  \"frequency_penalty\": 0.0,\n" +
               "  \"presence_penalty\": 0.0\n" +
               "}";
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String parseAzureResponse(String responseBody) {
        try {
            // Simple JSON parsing for the response
            int contentStart = responseBody.indexOf("\"content\":\"");
            if (contentStart != -1) {
                contentStart += 12; // length of "\"content\":\""
                int contentEnd = responseBody.indexOf("\"", contentStart);
                if (contentEnd != -1) {
                    return responseBody.substring(contentStart, contentEnd)
                                   .replace("\\n", "\n")
                                   .replace("\\\"", "\"");
                }
            }
            return "Sorry, I couldn't generate a recommendation at this time.";
        } catch (Exception e) {
            return "Sorry, I couldn't parse the response at this time.";
        }
    }

    private String buildPrompt(RecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Based on the following user preferences, recommend 3 restaurants:\n\n");
        
        if (request.getUserPreference() != null && !request.getUserPreference().trim().isEmpty()) {
            prompt.append("User Preference: ").append(request.getUserPreference()).append("\n\n");
        }

        if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
            prompt.append("Location: ").append(request.getLocation()).append("\n\n");
        }
        
        if (request.getPriceRange() != null && !request.getPriceRange().trim().isEmpty()) {
            prompt.append("Price Range: ").append(request.getPriceRange()).append("\n\n");
        }
        
        prompt.append("Please provide a helpful recommendation with reasoning for each restaurant.");
        
        return prompt.toString();
    }
}
