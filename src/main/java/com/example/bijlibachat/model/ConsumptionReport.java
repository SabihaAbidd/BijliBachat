package com.example.bijlibachat.model;

import java.util.Date;
import java.util.List;

public class ConsumptionReport {
    private int reportID;
    private int userID;
    private Date generatedDate;
    private String period;
    private float totalUnits;
    private Date startDate;
    private Date endDate;
    private float estimatedCost;
    private List<MeterReading> readings;
    private String csvPath;
    private String pdfPath;

    public int getReportID()                         { return reportID; }
    public void setReportID(int reportID)            { this.reportID = reportID; }
    public int getUserID()                           { return userID; }
    public void setUserID(int userID)                { this.userID = userID; }
    public Date getGeneratedDate()                   { return generatedDate; }
    public void setGeneratedDate(Date generatedDate) { this.generatedDate = generatedDate; }
    public String getPeriod()                        { return period; }
    public void setPeriod(String period)             { this.period = period; }
    public float getTotalUnits()                     { return totalUnits; }
    public void setTotalUnits(float totalUnits)      { this.totalUnits = totalUnits; }
    public Date getStartDate()                       { return startDate; }
    public void setStartDate(Date startDate)         { this.startDate = startDate; }
    public Date getEndDate()                         { return endDate; }
    public void setEndDate(Date endDate)             { this.endDate = endDate; }
    public float getEstimatedCost()                  { return estimatedCost; }
    public void setEstimatedCost(float estimatedCost){ this.estimatedCost = estimatedCost; }
    public List<MeterReading> getReadings()          { return readings; }
    public void setReadings(List<MeterReading> readings) { this.readings = readings; }
    public String getCsvPath()                       { return csvPath; }
    public void setCsvPath(String csvPath)           { this.csvPath = csvPath; }
    public String getPdfPath()                       { return pdfPath; }
    public void setPdfPath(String pdfPath)           { this.pdfPath = pdfPath; }

    public ConsumptionReport create(List<MeterReading> readings, String type) { return null; }
    public Object export(String format)              { return null; }
}
