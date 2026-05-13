package com.example.bijlibachat.model;

public class MonthlyBudget {
    private int budgetID;
    private int userID;
    private float ceilingAmount;
    private String month;

    public int getBudgetID()                     { return budgetID; }
    public void setBudgetID(int budgetID)        { this.budgetID = budgetID; }
    public int getUserID()                       { return userID; }
    public void setUserID(int userID)            { this.userID = userID; }
    public float getCeilingAmount()              { return ceilingAmount; }
    public void setCeilingAmount(float ceiling)  { this.ceilingAmount = ceiling; }
    public String getMonth()                     { return month; }
    public void setMonth(String month)           { this.month = month; }

    public MonthlyBudget getCeilingProgress()    { return this; }
    public void setCeiling(float units)          { this.ceilingAmount = units; }
}
