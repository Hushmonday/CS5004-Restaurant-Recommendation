package com.restaurant.recommendation.model;

public class RecommendationRequest {
    private String userPreference;
    private String location;
    private String priceRange;

    public RecommendationRequest() {}

    public RecommendationRequest(String userPreference, String location, String priceRange) {
        this.userPreference = userPreference;
        this.location = location;
        this.priceRange = priceRange;
    }

    // Getters
    public String getUserPreference() { return userPreference; }
    public String getLocation() { return location; }
    public String getPriceRange() { return priceRange; }

    // Setters
    public void setUserPreference(String userPreference) { this.userPreference = userPreference; }
    public void setLocation(String location) { this.location = location; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
}
