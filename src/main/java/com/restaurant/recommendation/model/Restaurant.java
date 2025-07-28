package com.restaurant.recommendation.model;

public class Restaurant {
    private Long id;
    private String name;
    private String cuisine;
    private String location;
    private Double rating;
    private String description;
    private String priceRange;
    private String address;
    private String phone;
    private String openingHours;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getLocation() { return location; }
    public Double getRating() { return rating; }
    public String getDescription() { return description; }
    public String getPriceRange() { return priceRange; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getOpeningHours() { return openingHours; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    public void setLocation(String location) { this.location = location; }
    public void setRating(Double rating) { this.rating = rating; }
    public void setDescription(String description) { this.description = description; }
    public void setPriceRange(String priceRange) { this.priceRange = priceRange; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
}
