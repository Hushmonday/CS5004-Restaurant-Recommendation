package com.restaurant.recommendation.service;

public abstract class BaseService {

  protected void logInfo(String message) {
    System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
  }

  protected void logError(String message, Exception e) {
    System.err.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    if (e != null) {
      e.printStackTrace();
    }
  }

  protected void logDebug(String message) {
    System.out.println("[" + this.getClass().getSimpleName() + "] DEBUG: " + message);
  }

  // 抽象方法，子类必须实现
  public abstract boolean isServiceHealthy();
  public abstract String getServiceName();
}