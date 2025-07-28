#!/bin/bash

# Run Pure Java Restaurant Recommendation Server

echo "Starting Pure Java Restaurant Recommendation Server..."

# Compile if needed
if [ ! -d "build" ]; then
    echo "Compiling first..."
    ./compile.sh
fi

# Run the server
java -cp build com.restaurant.recommendation.core.RestaurantRecommendationServer 