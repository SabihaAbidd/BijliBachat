package com.example.bijlibachat.model;

public class ElectricityProvider {
    private int providerID;
    private String name;
    private String region;

    public int getProviderID()               { return providerID; }
    public void setProviderID(int id)        { this.providerID = id; }
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getRegion()                { return region; }
    public void setRegion(String region)     { this.region = region; }

    public ElectricityProvider getProviderInfo() { return this; }
}