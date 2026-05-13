package com.example.bijlibachat.model;

import java.util.Date;

public class SolarMeter {
    private int solarID;
    private int userID;
    private float generationReading;
    private float exportedUnits;
    private float netCredit;
    private Date readingDate;

    public int getSolarID()                          { return solarID; }
    public void setSolarID(int solarID)              { this.solarID = solarID; }
    public int getUserID()                           { return userID; }
    public void setUserID(int userID)                { this.userID = userID; }
    public float getGenerationReading()              { return generationReading; }
    public void setGenerationReading(float g)        { this.generationReading = g; }
    public float getExportedUnits()                  { return exportedUnits; }
    public void setExportedUnits(float e)            { this.exportedUnits = e; }
    public float getNetCredit()                      { return netCredit; }
    public void setNetCredit(float netCredit)        { this.netCredit = netCredit; }
    public Date getReadingDate()                     { return readingDate; }
    public void setReadingDate(Date readingDate)     { this.readingDate = readingDate; }

    public float calculateNetConsumption()           { return 0.0f; }
    public float deductNetMeteringCredit(float units){ return 0.0f; }
    public SolarMeter getSolarStats()                { return this; }
}
