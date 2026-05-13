package com.example.bijlibachat.model;

public class User {
    private int userID;
    private String name;
    private String mail;
    private String password;
    private String phone;
    private String accountType;

    public int getUserID()                   { return userID; }
    public void setUserID(int userID)        { this.userID = userID; }
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getMail()                  { return mail; }
    public void setMail(String mail)         { this.mail = mail; }
    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getPhone()                 { return phone; }
    public void setPhone(String phone)       { this.phone = phone; }
    public String getAccountType()           { return accountType; }
    public void setAccountType(String t)     { this.accountType = t; }

    public User getProfile(int userID)           { return null; }
    public boolean validate(Object changes)      { return false; }
    public void saveChanges(Object changes)      { }
    public boolean authenticate(String email, String pwd) { return false; }
    public boolean validateInfo()                { return false; }
    public boolean checkDuplicate()              { return false; }
}
