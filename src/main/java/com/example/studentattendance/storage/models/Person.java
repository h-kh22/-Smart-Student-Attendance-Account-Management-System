package com.example.studentattendance.models;

public class Person {
    private String name;
    private String mobileNumber;
    private String residenceArea;

    public Person(String name, String mobileNumber, String residenceArea) {
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.residenceArea = residenceArea;
    }

    public Person() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getResidenceArea() {
        return residenceArea;
    }

    public void setResidenceArea(String residenceArea) {
        this.residenceArea = residenceArea;
    }

    @Override
    public String toString() {
        return "Name: " + name +
                "\nMobile Number: " + mobileNumber +
                "\nResidence Area: " + residenceArea;
    }
}
