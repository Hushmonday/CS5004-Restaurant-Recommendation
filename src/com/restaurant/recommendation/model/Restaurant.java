package com.restaurant.recommendation.model;

public class Restaurant {
    private Long id;
    private String name;
    private String cuisine;
    private String location;
    private Double rating;
    private String description;
    private String priceRange;

    public Restaurant() {}

    public Restaurant(Long id, String name, String cuisine, String location, Double rating, String description, String priceRange) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.location = location;
        this.rating = rating;
        this.description = description;
        this.priceRange = priceRange;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getLocation() { return location; }
    public Double getRating() { return rating; }
    public String getDescription() { return description; }
    public String getPriceRange() { return priceRange; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setLocation(String location) { this.location = location; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setDescription(String description) { this.description = description; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
}
