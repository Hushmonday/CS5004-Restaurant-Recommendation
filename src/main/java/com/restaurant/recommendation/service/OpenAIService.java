package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatChoice;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.ai.openai.models.ChatResponseMessage;
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
            }
        } catch (IOException e) {
            System.err.println("Error loading properties: " + e.getMessage());
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
                return "Error: Azure OpenAI client not initialized. Please check configuration.";
            }

            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestSystemMessage("You are a helpful restaurant recommendation assistant. Provide friendly and helpful restaurant suggestions."));
            chatMessages.add(new ChatRequestUserMessage(buildPrompt(request)));

            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
            chatCompletionsOptions.setTemperature(0.7);
            chatCompletionsOptions.setTopP(1.0);
            chatCompletionsOptions.setFrequencyPenalty(0.0);
            chatCompletionsOptions.setPresencePenalty(0.0);

            ChatCompletions chatCompletions = client.getChatCompletions(azureDeployment, chatCompletionsOptions);

            if (chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty()) {
                ChatChoice choice = chatCompletions.getChoices().get(0);
                ChatResponseMessage message = choice.getMessage();
                return message.getContent();
            } else {
                return "Sorry, I couldn't generate a recommendation at this time.";
            }

        } catch (Exception e) {
            System.err.println("Error getting recommendation from Azure OpenAI: " + e.getMessage());
            return "Sorry, we are temporarily unable to recommend restaurants. Please try again later. Error: " + e.getMessage();
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
