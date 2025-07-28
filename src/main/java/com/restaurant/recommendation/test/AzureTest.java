package com.restaurant.recommendation.test;

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

import java.util.ArrayList;
import java.util.List;

public class AzureTest {
    public static void main(String[] args) {
        String apiKey = "FPHHFpog4Gqh21OqxCrgfyLgGQxxeta9xdm558Q43hEys5M22E5wJQQJ99BGACfhMk5XJ3w3AAAAACOGNLle";
        String endpoint = "https://peiha-mde4efpc-swedencentral.cognitiveservices.azure.com/";
        String deploymentName = "pleydish";

        try {
            OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();

            List<ChatRequestMessage> chatMessages = new ArrayList<>();
            chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
            chatMessages.add(new ChatRequestUserMessage("Hello, can you recommend a restaurant?"));

            ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(chatMessages);
            chatCompletionsOptions.setTemperature(0.7);
            chatCompletionsOptions.setTopP(1.0);
            chatCompletionsOptions.setFrequencyPenalty(0.0);
            chatCompletionsOptions.setPresencePenalty(0.0);

            System.out.println("Testing Azure OpenAI connection...");
            ChatCompletions chatCompletions = client.getChatCompletions(deploymentName, chatCompletionsOptions);

            if (chatCompletions.getChoices() != null && !chatCompletions.getChoices().isEmpty()) {
                ChatChoice choice = chatCompletions.getChoices().get(0);
                ChatResponseMessage message = choice.getMessage();
                System.out.println("✅ Success! Response: " + message.getContent());
            } else {
                System.out.println("❌ No response received");
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 