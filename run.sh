#!/bin/bash

echo "Starting Restaurant Recommendation System..."
echo "Please ensure you have configured the correct OpenAI API Key in application.properties"
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java not found, please install Java first"
    exit 1
fi

echo "Java version:"
java -version
echo ""

# Try to start with gradle
echo "Attempting to start with Gradle..."
if ./gradlew bootRun; then
    echo "Application started successfully!"
    echo "Visit http://localhost:8080/api/recommendations/health to test the service"
    echo "Visit http://localhost:8080/api/recommendations/test-openai to test AI functionality"
else
    echo "Gradle startup failed, please check configuration"
fi 