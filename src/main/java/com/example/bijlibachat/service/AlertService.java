package com.example.bijlibachat.service;

import java.util.ArrayList;
import java.util.List;

public class AlertService {
    private List<ConsumptionObserver> observers = new ArrayList<>();

    public void addObserver(ConsumptionObserver o) {
        observers.add(o);
    }

    public void removeObserver(ConsumptionObserver o) {
        observers.remove(o);
    }

    public void monitorConsumption(float currentUnits, float ceiling) {
        float percentage = (currentUnits / ceiling) * 100;

        if (percentage >= 100) {
            notifyCeilingExceeded(currentUnits, ceiling);
        } else if (percentage >= 80) {
            float costRisk = estimateCostRisk(currentUnits);
            notifyThresholdReached(currentUnits, ceiling, costRisk);
        }
    }

    public void checkTariffTierAlert(float currentUnits) {
        // Alert when within 20 units of next tier boundary
        float[] boundaries = {100, 200, 300};
        for (float boundary : boundaries) {
            float distance = boundary - currentUnits;
            if (distance > 0 && distance <= 20) {
                System.out.println("ALERT: You are " + distance + " units away from the next tariff tier!");
            }
        }
    }

    private void notifyThresholdReached(float consumption, float threshold, float costRisk) {
        for (ConsumptionObserver o : observers) {
            o.onThresholdReached(consumption, threshold, costRisk);
        }
    }

    private void notifyCeilingExceeded(float consumption, float ceiling) {
        for (ConsumptionObserver o : observers) {
            o.onCeilingExceeded(consumption, ceiling);
        }
    }

    private float estimateCostRisk(float units) {
        return units * 19.55f; // worst case: all at top tier
    }
}
