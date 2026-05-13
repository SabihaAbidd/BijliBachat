package com.example.bijlibachat.model;

import java.util.Date;
import java.util.List;

public class MeterReading {
    private int readingID;
    private int userID;
    private float value;
    private Date date;
    private float unitsConsumed;

    public int getReadingID()                        { return readingID; }
    public void setReadingID(int readingID)          { this.readingID = readingID; }
    public int getUserID()                           { return userID; }
    public void setUserID(int userID)                { this.userID = userID; }
    public float getValue()                          { return value; }
    public void setValue(float value)                { this.value = value; }
    public Date getDate()                            { return date; }
    public void setDate(Date date)                   { this.date = date; }
    public float getUnitsConsumed()                  { return unitsConsumed; }
    public void setUnitsConsumed(float unitsConsumed){ this.unitsConsumed = unitsConsumed; }

    public float getCurrentConsumption()                        { return unitsConsumed; }
    public List<MeterReading> getReadingsByPeriod(Date s, Date e) { return null; }
    public float calculateUnitsConsumed()                       { return 0.0f; }
    public void saveReading()                                   { }
    public MeterReading getLastReading()                        { return null; }
    public Object getConsumptionChart()                         { return null; }
}
