package com.example.studentattendance.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayList;

public class Student {
    private int ID;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    public Student() {
    }

    @Override
    public String toString() {
        return ID +","+ studentName +","+ mobileNumber +","+ residenceArea ;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getResidenceArea() {
        return residenceArea;
    }

    public void setResidenceArea(String residenceArea) {
        this.residenceArea = residenceArea;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    private String studentName;

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    private String mobileNumber;

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void setCourses(ArrayList<String> courses) {
        this.courses = courses;
    }
    public boolean isSelected() {
        return selected.get();
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    private String residenceArea;
    private ArrayList<String> courses;

    public Student(int ID, String studentName, String mobileNumber, String residenceArea) {
        this.ID = ID;
        this.studentName = studentName;
        this.mobileNumber = mobileNumber;
        this.residenceArea = residenceArea;
    }
}
