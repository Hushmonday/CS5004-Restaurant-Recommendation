package com.restaurant.recommendation.model;

import java.util.List;

public class RecommendationResponse {
    private List<Restaurant> recommendations;
    private String aiExplanation;
    private String reasoning;

    // Getters
    public List<Restaurant> getRecommendations() { return recommendations; }
    public String getAiExplanation() { return aiExplanation; }
    public String getReasoning() { return reasoning; }

    // Setters
    public void setRecommendations(List<Restaurant> recommendations) { this.recommendations = recommendations; }
    public void setAiExplanation(String aiExplanation) { this.aiExplanation = aiExplanation; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
}
