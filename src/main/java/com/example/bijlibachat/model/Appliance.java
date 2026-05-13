package com.example.bijlibachat.model;

import java.util.List;

public class Appliance {
    private int applianceID;
    private int userID;
    private String name;
    private float wattage;
    private float hoursUsed;

    public int getApplianceID()              { return applianceID; }
    public void setApplianceID(int id)       { this.applianceID = id; }
    public int getUserID()                   { return userID; }
    public void setUserID(int userID)        { this.userID = userID; }
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public float getWattage()                { return wattage; }
    public void setWattage(float wattage)    { this.wattage = wattage; }
    public float getHoursUsed()              { return hoursUsed; }
    public void setHoursUsed(float hours)    { this.hoursUsed = hours; }

    public void recordUsage()                        { }
    public List<Appliance> getAllAppliances()         { return null; }
    public Appliance getBreakdown(int applianceID)   { return null; }
    public float getLiveReading(int id)              { return (wattage / 1000) * hoursUsed; }
}
