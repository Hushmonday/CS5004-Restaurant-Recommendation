package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OpenAIService {
    private String azureApiKey;
    private String azureEndpoint;
    private String azureDeployment;
    private OpenAIClient client;

    public OpenAIService() {
        loadProperties();
        initializeClient();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                azureApiKey = props.getProperty("azure.openai.api.key");
                azureEndpoint = props.getProperty("azure.openai.endpoint");
                azureDeployment = props.getProperty("azure.openai.deployment");

                // Add debug output
                System.out.println("=== Configuration Loading Check ===");
                System.out.println("Config file found: Yes");
                System.out.println("API Key loaded: " + (azureApiKey != null ? "Success (***" + azureApiKey.substring(azureApiKey.length() - 4) + ")" : "Failed"));
                System.out.println("Endpoint: " + azureEndpoint);
                System.out.println("Deployment: " + azureDeployment);
                System.out.println("==================================");
            } else {
                System.err.println("Error: application.properties file not found!");
                System.err.println("Please ensure the file is in src/main/resources/ directory");
            }
        } catch (IOException e) {
            System.err.println("Error loading configuration file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeClient() {
        if (azureApiKey != null && azureEndpoint != null) {
            client = new OpenAIClientBuilder()
                    .credential(new AzureKeyCredential(azureApiKey))
                    .endpoint(azureEndpoint)
                    .buildClient();
        }
    }

    public String getRecommendation(RecommendationRequest request) {
        try {
            if (client == null) {
                return "Error: Azure OpenAI client not initialized. Check your API key and endpoint.";
            }

            List<ChatRequestMessage> chatMessages = new ArrayList<>();

            // More specific system message for restaurant recommendations
            String systemMessage = "You are a restaurant recommendation assistant specializing in local restaurants. " +
                    "IMPORTANT RULES:\n" +
                    "1. ALWAYS recommend restaurants that are actually in the location specified by the user\n" +
                    "2. If a user mentions a specific city (like San Jose), ONLY recommend restaurants in that city\n" +
                    "3. Focus on matching the user's food preferences (like 'spicy food')\n" +
                    "4. Provide exactly 3 restaurant recommendations\n" +
                    "5. Use real restaurant names and accurate information\n" +
                    "6. Format each recommendation exactly as: Name | Cuisine Type | City/Location | Rating (1-5) | Price ($-$$$$) | Description\n" +
                    "7. Never recommend restaurants from other cities unless explicitly asked\n" +
                    "8. For 'spicy food' requests, focus on cuisines known for spicy dishes (Thai, Indian, Sichuan, Mexican, Korean, etc.)\n" +
                    "Always respond in English.";

            chatMessages.add(new ChatRequestSystemMessage(systemMessage));

            // Create a more specific prompt based on the request
            String userMessage = createLocationAwarePrompt(request);
            chatMessages.add(new ChatRequestUserMessage(userMessage));

            // Log what we're sending (for debugging)
            System.out.println("=== AI Request Debug ===");
            System.out.println("User Input: " + request.getUserPreference());
            System.out.println("Location: " + request.getLocation());
            System.out.println("Sending to AI: " + userMessage);
            System.out.println("======================");

            ChatCompletionsOptions options = new ChatCompletionsOptions(chatMessages);
            options.setTemperature(0.7); // Lower temperature for more consistent location-based results
            options.setTopP(0.9);
            options.setMaxTokens(500);

            ChatCompletions chatCompletions = client.getChatCompletions(azureDeployment, options);
            if (chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty()) {
                String response = chatCompletions.getChoices().get(0).getMessage().getContent();
                System.out.println("=== AI Response ===");
                System.out.println(response);
                System.out.println("==================");
                return response;
            }
            return "No AI recommendation received.";
        } catch (Exception e) {
            System.err.println("=== AI Error ===");
            e.printStackTrace();
            return "AI error: " + e.getMessage();
        }
    }

    private String createLocationAwarePrompt(RecommendationRequest r) {
        String userInput = r.getUserPreference();
        String location = r.getLocation();

        // Extract location from user input if not explicitly set
        if ((location == null || location.trim().isEmpty()) && userInput != null) {
            // Check if user input contains location information
            String lowerInput = userInput.toLowerCase();
            if (lowerInput.contains("san jose")) {
                location = "San Jose, CA";
            } else if (lowerInput.contains("at ") || lowerInput.contains("in ")) {
                // Try to extract location after "at" or "in"
                int atIndex = lowerInput.indexOf(" at ");
                int inIndex = lowerInput.indexOf(" in ");
                int index = Math.max(atIndex, inIndex);
                if (index > 0) {
                    location = userInput.substring(index + 4).trim();
                }
            }
        }

        // Build a clear, specific prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("User request: ");

        if (userInput != null && !userInput.trim().isEmpty()) {
            prompt.append("\"").append(userInput).append("\"\n\n");

            // Parse specific requirements from the input
            String lowerInput = userInput.toLowerCase();
            if (lowerInput.contains("spicy")) {
                prompt.append("The user specifically wants SPICY food. Focus on restaurants known for spicy dishes.\n");
                prompt.append("Good options include: Thai, Indian (especially South Indian), Sichuan Chinese, Korean, Mexican restaurants.\n");
            }
        }

        if (location != null && !location.trim().isEmpty()) {
            prompt.append("LOCATION REQUIREMENT: You MUST only recommend restaurants in ").append(location).append(".\n");
            prompt.append("Do NOT recommend restaurants from any other city.\n\n");
        }

        prompt.append("Please recommend exactly 3 restaurants that match these requirements.\n");
        prompt.append("Format each restaurant as:\n");
        prompt.append("1. Restaurant Name | Cuisine Type | City/Location | Rating (1-5) | Price ($-$$$$) | Why it matches the request\n");
        prompt.append("2. (same format)\n");
        prompt.append("3. (same format)\n\n");
        prompt.append("Make sure all restaurants are real establishments in the specified location.");

        return prompt.toString();
    }

    private String createFlexiblePrompt(RecommendationRequest r) {
        // Use the new location-aware prompt instead
        return createLocationAwarePrompt(r);
    }

    private String buildPrompt(RecommendationRequest r) {
        return createLocationAwarePrompt(r);
    }

    private String safe(String s) {
        return (s != null && !s.isEmpty()) ? s : "Not specified";
    }
}