package com.example.studentattendance.models;

import java.io.Serializable;

public class Teacher extends Person implements Serializable {
    private String subject;
    private String book;
    private String hall;
    private String username;
    private String password;


    public Teacher(int code, String username, String password) {
        this.username = username;
        this.password = password;

    }

    public Teacher(String name, String mobileNumber, String residenceArea, String subject, String book, String hall) {
        super(name, mobileNumber, residenceArea);
        this.subject = subject;
        this.book = book;
        this.hall = hall;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public String getHall() {
        return hall;
    }

    public void setHall(String virtualMeetingPlace) {
        this.hall = virtualMeetingPlace;
    }
}


