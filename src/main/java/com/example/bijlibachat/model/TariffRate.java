package com.example.bijlibachat.model;

import java.util.Date;

public class TariffRate {
    private int tariffID;
    private String tierName;
    private float ratePerUnit;
    private float fixedCharge;
    private int minUnits;
    private int maxUnits;
    private Date lastUpdated;

    public int getTariffID()                     { return tariffID; }
    public void setTariffID(int tariffID)        { this.tariffID = tariffID; }
    public String getTierName()                  { return tierName; }
    public void setTierName(String tierName)     { this.tierName = tierName; }
    public float getRatePerUnit()                { return ratePerUnit; }
    public void setRatePerUnit(float rate)       { this.ratePerUnit = rate; }
    public float getFixedCharge()                { return fixedCharge; }
    public void setFixedCharge(float fixedCharge){ this.fixedCharge = fixedCharge; }
    public int getMinUnits()                     { return minUnits; }
    public void setMinUnits(int minUnits)        { this.minUnits = minUnits; }
    public int getMaxUnits()                     { return maxUnits; }
    public void setMaxUnits(int maxUnits)        { this.maxUnits = maxUnits; }
    public Date getLastUpdated()                 { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public float getThreshold()                      { return maxUnits; }
    public TariffRate getTierDetails()               { return this; }
    public float applyTariffStructure(float units)   { return 0.0f; }
    public TariffRate getActiveTariffTier(float units) { return null; }
    public boolean validateTariffValues()            { return ratePerUnit > 0; }
    public void recalculateAllBillEstimates()        { }
}
