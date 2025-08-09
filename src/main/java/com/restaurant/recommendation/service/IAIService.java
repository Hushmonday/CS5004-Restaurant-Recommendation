package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;

public interface IAIService {
  String getRecommendation(RecommendationRequest request);
  void resetConversation();
  String getCurrentLocation();
  boolean isAvailable();
}
