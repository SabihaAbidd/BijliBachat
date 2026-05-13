package com.example.bijlibachat.model;

import java.util.List;

public class ElectricityBill {
    private int billID;
    private int userID;
    private float estimatedAmount;
    private String billingCycle;
    private String status;

    public int getBillID()                           { return billID; }
    public void setBillID(int billID)                { this.billID = billID; }
    public int getUserID()                           { return userID; }
    public void setUserID(int userID)                { this.userID = userID; }
    public float getEstimatedAmount()                { return estimatedAmount; }
    public void setEstimatedAmount(float amt)        { this.estimatedAmount = amt; }
    public String getBillingCycle()                  { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
    public String getStatus()                        { return status; }
    public void setStatus(String status)             { this.status = status; }

    public float includeFixedChargesAndTaxes(float amt) { return 0.0f; }
    public void calculateAndRecordBill()                { }
    public float getLatestEstimate()                    { return 0.0f; }
    public List<ElectricityBill> getBillingHistory()    { return null; }
}
