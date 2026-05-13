package com.example.bijlibachat.service;

public class GridTariffStrategy implements TariffStrategy {
    @Override
    public float applyTariffStructure(float units) {
        if (units <= 100)
            return units * 5.79f;
        if (units <= 200)
            return (100 * 5.79f) + ((units - 100) * 8.11f);
        if (units <= 300)
            return (100 * 5.79f) + (100 * 8.11f) + ((units - 200) * 12.42f);
        return (100 * 5.79f) + (100 * 8.11f) + (100 * 12.42f) + ((units - 300) * 19.55f);
    }
}
