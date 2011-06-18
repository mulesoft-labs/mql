package com.mulesoft.mql;

public class User {
    private String firstName;
    private String lastName;
    private int income;
    private String division;
    private String twitterId;

    public User() {
    }

    public User(String firstName, String lastName, String division, int income) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.income = income;
        this.division = division;
    }

    public User(String firstName, String lastName, String division, int income, String twitterId) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.income = income;
        this.division = division;
        this.twitterId = twitterId;
    }

    public User(String string, String string2, String string3, String string4) {
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

}
