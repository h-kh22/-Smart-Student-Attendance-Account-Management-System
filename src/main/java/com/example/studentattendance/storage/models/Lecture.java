package com.example.studentattendance.models;

import com.example.studentattendance.DataModel;

public class Lecture {
    DataModel.AccountDataModel accountDataModel = new DataModel.AccountDataModel();
    private int lCode;

    public int getTeacher_Code() {
        int code = accountDataModel.getCodeByUsername(teacher_name);
        return code != -1 ? code : teacher_Code;
    }

    public void setTeacher_Code(int teacher_Code) {
        this.teacher_Code = teacher_Code;
    }

    private int teacher_Code;

    public String getTeacher_name() {
        String name = accountDataModel.getUsernameByCode(teacher_Code);
        return name != null ? name : teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    private String teacher_name;
    private String lName;
    private String classroom;

    Account account = new Account();
    public Lecture(){
    }

    public Lecture(int lCode,int teacher_Code,String teacher_name, String lName, String classroom) {
        this.lCode = lCode;
        this.teacher_Code=teacher_Code;
        this.teacher_name=teacher_name;
        this.lName = lName;
        this.classroom = classroom;
    }

    public int getLCode() {
        return lCode;
    }

    public void setLCode(int lCode) {
        this.lCode = lCode;
    }

    public String getLName() {
        return lName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
    }


    @Override
    public String toString() {
        return lCode+","+teacher_Code+","+teacher_name+","+lName+","+classroom;
    }
}
