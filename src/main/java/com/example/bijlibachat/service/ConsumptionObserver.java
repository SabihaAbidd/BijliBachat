package com.example.bijlibachat.service;

public interface ConsumptionObserver {
    void onThresholdReached(float consumption, float threshold, float costRisk);
    void onCeilingExceeded(float consumption, float ceiling);
}
