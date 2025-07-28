#!/bin/bash

# Pure Java Restaurant Recommendation Backend
# Compilation Script

echo "Compiling Pure Java Restaurant Recommendation Backend..."

# Create output directory
mkdir -p build

# Compile all Java files
javac -d build -cp "src" src/com/restaurant/recommendation/model/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/service/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/controller/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/core/*.java
javac -d build -cp "src" src/com/restaurant/recommendation/view/*.java

# Copy resources
cp application.properties build/

echo "Compilation completed!"
echo "To run the server: java -cp build com.restaurant.recommendation.core.RestaurantRecommendationServer"
echo "To run the UI: java -cp build com.restaurant.recommendation.view.RestaurantChatUI" 