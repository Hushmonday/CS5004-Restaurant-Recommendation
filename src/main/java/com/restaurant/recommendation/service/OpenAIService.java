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

public class OpenAIService extends BaseService implements IAIService {
    private String azureApiKey;
    private String azureEndpoint;
    private String azureDeployment;
    private OpenAIClient client;

    // Add conversation history storage
    private List<ChatRequestMessage> conversationHistory = new ArrayList<>();
    private String currentUserLocation = null; // Store user location

    public OpenAIService() {
        loadProperties();
        initializeClient();
        initializeConversation();
        logInfo("OpenAI Service initialized");
    }

    @Override
    public String getRecommendation(RecommendationRequest request) {
        logInfo("Getting AI recommendation");

        try {
            if (client == null) {
                return "Error: Azure OpenAI client not initialized. Please check API key and endpoint.";
            }

            String userInput = request.getUserPreference();
            extractAndSaveLocation(userInput);

            if (request.getLocation() != null && !request.getLocation().trim().isEmpty()) {
                currentUserLocation = request.getLocation();
            }

            String contextualMessage = createContextualPrompt(request);
            conversationHistory.add(new ChatRequestUserMessage(contextualMessage));

            logDebug("Current saved location: " + currentUserLocation);
            logDebug("User input: " + userInput);
            logDebug("Conversation history length: " + conversationHistory.size());

            ChatCompletionsOptions options = new ChatCompletionsOptions(conversationHistory);
            options.setTemperature(0.7);
            options.setTopP(0.9);
            options.setMaxTokens(500);

            ChatCompletions chatCompletions = client.getChatCompletions(azureDeployment, options);
            if (chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty()) {
                String response = chatCompletions.getChoices().get(0).getMessage().getContent();

                conversationHistory.add(new ChatRequestAssistantMessage(response));

                // Limit conversation history length to avoid token limit
                if (conversationHistory.size() > 20) {
                    List<ChatRequestMessage> trimmedHistory = new ArrayList<>();
                    trimmedHistory.add(conversationHistory.get(0)); // System message
                    trimmedHistory.addAll(conversationHistory.subList(conversationHistory.size() - 15, conversationHistory.size()));
                    conversationHistory = trimmedHistory;
                }

                logInfo("AI response received successfully");
                return response;
            }
            return "No AI recommendation received.";
        } catch (Exception e) {
            logError("Error getting AI recommendation", e);
            return "AI error: " + e.getMessage();
        }
    }

    @Override
    public void resetConversation() {
        conversationHistory.clear();
        currentUserLocation = null;
        initializeConversation();
        logInfo("Conversation history has been reset");
    }

    @Override
    public String getCurrentLocation() {
        return currentUserLocation;
    }

    @Override
    public boolean isAvailable() {
        return client != null && azureApiKey != null && azureEndpoint != null;
    }

    @Override
    public boolean isServiceHealthy() {
        return isAvailable();
    }

    @Override
    public String getServiceName() {
        return "Azure OpenAI Service";
    }

    // Initialize conversation with system message
    private void initializeConversation() {
        String systemMessage = "You are a friendly and helpful AI assistant who can help with restaurant recommendations and general conversation. " +
            "When helping with restaurants:\n" +
            "- Remember location information the user provides (don't ask repeatedly)\n" +
            "- Only recommend restaurants in their specified location\n" +
            "- Match their taste preferences and dietary needs\n" +
            "- Provide helpful details about restaurants\n" +
            "\nConversation style:\n" +
            "- Be conversational and natural\n" +
            "- Answer follow-up questions about restaurants or other topics\n" +
            "- If they ask non-restaurant questions, feel free to help with those too\n" +
            "- Ask clarifying questions when helpful\n" +
            "- Be concise but informative\n" +
            "\nRemember: You're having a conversation with a person, not just generating restaurant lists!";

        conversationHistory.add(new ChatRequestSystemMessage(systemMessage));
    }

    private void extractAndSaveLocation(String userInput) {
        if (userInput == null) return;

        String lowerInput = userInput.toLowerCase();

        boolean isFoodRelated = lowerInput.contains("food") || lowerInput.contains("restaurant") ||
            lowerInput.contains("eat") || lowerInput.contains("dining") ||
            lowerInput.contains("spicy") || lowerInput.contains("cuisine");

        if (!isFoodRelated) return;

        if (lowerInput.contains("san jose")) {
            currentUserLocation = "San Jose, CA";
            logDebug("Detected user location: " + currentUserLocation);
        } else if (lowerInput.contains(" in ") || lowerInput.contains(" at ")) {
            int inIndex = lowerInput.indexOf(" in ");
            int atIndex = lowerInput.indexOf(" at ");
            int index = -1;

            if (inIndex > 0 && (atIndex == -1 || inIndex < atIndex)) {
                index = inIndex + 4;
            } else if (atIndex > 0) {
                index = atIndex + 4;
            }

            if (index > 0) {
                String extractedLocation = userInput.substring(index).trim();
                if (extractedLocation.length() > 2 && !extractedLocation.contains("?")) {
                    currentUserLocation = extractedLocation;
                    logDebug("Extracted user location: " + currentUserLocation);
                }
            }
        }
    }

    private String createContextualPrompt(RecommendationRequest request) {
        StringBuilder prompt = new StringBuilder();

        String userInput = request.getUserPreference();
        if (userInput != null && !userInput.trim().isEmpty()) {
            prompt.append(userInput);

            String lowerInput = userInput.toLowerCase();
            if (currentUserLocation != null && !currentUserLocation.trim().isEmpty() &&
                (lowerInput.contains("restaurant") || lowerInput.contains("food") ||
                    lowerInput.contains("eat") || lowerInput.contains("recommend"))) {
                prompt.append("\n\n[Context: User location is ").append(currentUserLocation).append("]");
            }
        }

        return prompt.toString();
    }

    private void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
                azureApiKey = props.getProperty("azure.openai.api.key");
                azureEndpoint = props.getProperty("azure.openai.endpoint");
                azureDeployment = props.getProperty("azure.openai.deployment");

                logInfo("Configuration loaded successfully");
                logDebug("API Key loaded: " + (azureApiKey != null ? "Success (***" + azureApiKey.substring(azureApiKey.length() - 4) + ")" : "Failed"));
                logDebug("Endpoint: " + azureEndpoint);
                logDebug("Deployment: " + azureDeployment);
            } else {
                logError("application.properties file not found!", null);
            }
        } catch (IOException e) {
            logError("Error loading configuration file", e);
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
}