package com.restaurant.recommendation.model;

public class RecommendationRequest {
    private String userPreference;
    private String location;
    private String cuisine;
    private String priceRange;
    private Integer numberOfPeople;
    private String occasion;

    // Getters
    public String getUserPreference() { return userPreference; }
    public String getLocation() { return location; }
    public String getCuisine() { return cuisine; }
    public String getPriceRange() { return priceRange; }
    public Integer getNumberOfPeople() { return numberOfPeople; }
    public String getOccasion() { return occasion; }

    // Setters
    public void setUserPreference(String userPreference) { this.userPreference = userPreference; }
    public void setLocation(String location) { this.location = location; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
    public void setNumberOfPeople(Integer numberOfPeople) { this.numberOfPeople = numberOfPeople; }
    public void setOccasion(String occasion) { this.occasion = occasion; }
}
