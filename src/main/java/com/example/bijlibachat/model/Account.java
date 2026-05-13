package com.example.bijlibachat.model;

import java.util.Date;

public class Account {
    private int accountID;
    private int userID;
    private String type;
    private String provider;
    private Date createdDate;

    public int getAccountID()                    { return accountID; }
    public void setAccountID(int accountID)      { this.accountID = accountID; }
    public int getUserID()                       { return userID; }
    public void setUserID(int userID)            { this.userID = userID; }
    public String getType()                      { return type; }
    public void setType(String type)             { this.type = type; }
    public String getProvider()                  { return provider; }
    public void setProvider(String provider)     { this.provider = provider; }
    public Date getCreatedDate()                 { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Account getSettings(int accountID)    { return null; }
    public void updateSettings(Object changes)   { }
    public Account createAccount()               { return null; }
}
