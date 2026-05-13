package com.example.bijlibachat.service;

import com.example.bijlibachat.model.TariffRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TariffCatalog {
    private final List<TariffRate> tariffs;
    private final String category;
    private final String sourceLabel;
    private final String sourceUrl;
    private final String effectiveDate;
    private final String lastSyncedAt;

    public TariffCatalog(List<TariffRate> tariffs,
                         String category,
                         String sourceLabel,
                         String sourceUrl,
                         String effectiveDate,
                         String lastSyncedAt) {
        this.tariffs = Collections.unmodifiableList(new ArrayList<>(tariffs));
        this.category = category;
        this.sourceLabel = sourceLabel;
        this.sourceUrl = sourceUrl;
        this.effectiveDate = effectiveDate;
        this.lastSyncedAt = lastSyncedAt;
    }

    public List<TariffRate> getTariffs() {
        return tariffs;
    }

    public String getCategory() {
        return category;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public String getLastSyncedAt() {
        return lastSyncedAt;
    }
}
