package com.mulesoft.mql;

public class Person {
    private String firstName;
    private String lastName;
    private int income;
    private String division;
    private String twitterId;

    public Person() {
    }

    public Person(String firstName, String lastName, String division, int income) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.income = income;
        this.division = division;
    }

    public Person(String firstName, String lastName, String division, int income, String twitterId) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.income = income;
        this.division = division;
        this.twitterId = twitterId;
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
