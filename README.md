# Restaurant Recommendation Backend System

An intelligent restaurant recommendation system based on Spring Boot and OpenAI.

## Features

- Smart restaurant recommendations (powered by OpenAI)
- RESTful API interface
- H2 in-memory database
- CORS support

## Quick Start

### 1. Configure OpenAI API Key

Set your OpenAI API key in `src/main/resources/application.properties`:

```properties
openai.api.key=your-openai-api-key-here
```

### 2. Run the Application

Import the project into your IDE, or use the Gradle command:

```bash
./gradlew bootRun
```

### 3. Test the API

#### Health Check
```bash
curl http://localhost:8080/api/recommendations/health
```

#### Get Restaurant Recommendations
```bash
curl -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "userPreference": "Likes spicy food",
    "location": "Beijing",
    "cuisine": "Sichuan",
    "priceRange": "Medium",
    "numberOfPeople": 4,
    "occasion": "Friends gathering"
  }'
```

## API Documentation

### POST /api/recommendations

Get restaurant recommendations

**Request Body:**
```json
{
  "userPreference": "User preference",
  "location": "Location",
  "cuisine": "Cuisine",
  "priceRange": "Price range",
  "numberOfPeople": Number of people,
  "occasion": "Occasion"
}
```

**Response:**
```json
{
  "recommendations": [
    {
      "id": 1,
      "name": "Restaurant Name",
      "cuisine": "Cuisine",
      "location": "Location",
      "rating": 4.5,
      "description": "Description",
      "priceRange": "Price range"
    }
  ],
  "aiExplanation": "AI recommendation reason",
  "reasoning": "Recommendation logic"
}
```

## Tech Stack

- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database
- OpenAI GPT-3 API
- Gradle
- Java 17

## Project Structure

```
src/main/java/com/restaurant/recommendation/
├── RestaurantRecommendationApplication.java
├── controller/
│   └── RecommendationController.java
├── service/
│   ├── OpenAIService.java
│   └── RestaurantService.java
└── model/
    ├── Restaurant.java
    ├── RecommendationRequest.java
    └── RecommendationResponse.java
```
