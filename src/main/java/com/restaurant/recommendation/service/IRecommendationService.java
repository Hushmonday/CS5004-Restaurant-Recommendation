package com.restaurant.recommendation.service;

import com.restaurant.recommendation.model.RecommendationRequest;
import com.restaurant.recommendation.model.RecommendationResponse;

public interface IRecommendationService {
  RecommendationResponse getRecommendations(RecommendationRequest request);
  void resetService();
}
