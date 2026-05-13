package com.example.bijlibachat.service;

import com.example.bijlibachat.model.TariffRate;

import java.util.List;

public class TariffEngine {
    private final String accountType;
    private final TariffSyncService tariffSyncService = new TariffSyncService();

    public TariffEngine(String accountType) {
        this.accountType = accountType == null ? "grid" : accountType;
    }

    public float applyTariffStructure(float units) {
        if ("solar".equalsIgnoreCase(accountType) && units <= 0) {
            return 0f;
        }

        float remainingUnits = Math.max(units, 0);
        float amount = 0f;
        for (TariffRate tariff : getAllTariffs()) {
            if (remainingUnits <= 0) {
                break;
            }

            int slabMin = Math.max(1, tariff.getMinUnits());
            int slabMax = tariff.getMaxUnits() <= 0 ? Integer.MAX_VALUE : tariff.getMaxUnits();
            float slabUnits = Math.min(remainingUnits, slabMax - slabMin + 1);
            if (slabUnits > 0) {
                amount += slabUnits * tariff.getRatePerUnit();
                remainingUnits -= slabUnits;
            }
        }
        return Math.round(amount * 100.0f) / 100.0f;
    }

    public float getFixedCharge(float units) {
        TariffRate activeTariff = getTariffForUnits(units);
        return activeTariff == null ? 0f : activeTariff.getFixedCharge();
    }

    public TariffCatalog getCatalog() {
        return tariffSyncService.getCatalog();
    }

    public TariffCatalog refreshCatalog() {
        return tariffSyncService.forceRefresh();
    }

    public boolean validateTariffValues(float rate) {
        return rate > 0;
    }

    public void recalculateAllBillEstimates() {
        // Called after tariff refresh; stored bill regeneration can be added later.
    }

    public List<TariffRate> getAllTariffs() {
        return getCatalog().getTariffs();
    }

    public TariffRate getTariffForUnits(float units) {
        float normalizedUnits = Math.max(units, 0);
        if (normalizedUnits <= 0) {
            return null;
        }
        for (TariffRate tariff : getAllTariffs()) {
            boolean aboveMin = normalizedUnits >= tariff.getMinUnits();
            boolean belowMax = tariff.getMaxUnits() <= 0 || normalizedUnits <= tariff.getMaxUnits();
            if (aboveMin && belowMax) {
                return tariff;
            }
        }
        List<TariffRate> tariffs = getAllTariffs();
        return tariffs.isEmpty() ? null : tariffs.get(tariffs.size() - 1);
    }

    public float getReferenceRate(float units) {
        TariffRate tariff = getTariffForUnits(units);
        return tariff == null ? 0f : tariff.getRatePerUnit();
    }

    public void updateTariff(TariffRate t) {
        throw new UnsupportedOperationException("Tariff rows are auto-synced from NEPRA.");
    }
}
