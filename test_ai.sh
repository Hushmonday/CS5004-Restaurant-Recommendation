#!/bin/bash

echo "🧪 Testing AI Restaurant Recommendation Functionality"
echo "=================================================="

# Test health check
echo "1. Testing service health status..."
HEALTH_RESPONSE=$(curl -s http://localhost:8080/api/recommendations/health)
if [ $? -eq 0 ]; then
    echo "✅ Service health check passed: $HEALTH_RESPONSE"
else
    echo "❌ Service health check failed"
    exit 1
fi

echo ""
echo "2. Testing AI recommendation functionality..."
AI_RESPONSE=$(curl -s http://localhost:8080/api/recommendations/test-openai)
if [ $? -eq 0 ]; then
    echo "✅ AI test request successful"
    echo "📝 AI response content:"
    echo "$AI_RESPONSE" | head -20
else
    echo "❌ AI test request failed"
    exit 1
fi

echo ""
echo "3. Testing POST recommendation endpoint..."
POST_RESPONSE=$(curl -s -X POST http://localhost:8080/api/recommendations \
  -H "Content-Type: application/json" \
  -d '{
    "userPreference": "Likes spicy food",
    "location": "Shanghai",
    "cuisine": "Sichuan",
    "priceRange": "Medium",
    "numberOfPeople": 4,
    "occasion": "Family dinner"
  }')

if [ $? -eq 0 ]; then
    echo "✅ POST recommendation endpoint test successful"
    echo "📝 Recommendation response:"
    echo "$POST_RESPONSE" | head -20
else
    echo "❌ POST recommendation endpoint test failed"
fi

echo ""
echo "🎉 Testing completed!" 