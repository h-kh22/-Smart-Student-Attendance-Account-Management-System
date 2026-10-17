package com.example.studentattendance.models;

import com.example.studentattendance.DataModel;
import javafx.scene.control.CheckBox;

public class Teacher_Student {
    private int student_ID;
    private String student_name;
    private int l_Code;
    private CheckBox selected;


    DataModel.AccountDataModel accountDataModel= new DataModel.AccountDataModel();
//    LectureDataModel lectureDataModel =new LectureDataModel();
public Teacher_Student(int student_ID, String student_name) {
    this.student_ID = student_ID;
    this.student_name = student_name;
   this.selected = new CheckBox();
}
 public CheckBox getSelected(){
    return selected;
 }
 public void setSelected(CheckBox selected){
    this.selected=selected;
 }
    public String getStudent_name() {
        return student_name;
    }

    public int getL_Code() {
        return l_Code;
    }

    public DataModel.AccountDataModel getAccountDataModel() {
        return accountDataModel;
    }

    public String getL_Name() {
        return l_Name;
    }

    public int getStudent_ID() {
        return student_ID;
    }

    public int getTeacher_Code() {
        return teacher_Code;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }



    public void setTeacher_Code(int teacher_Code) {
        this.teacher_Code = teacher_Code;
    }

    private int teacher_Code;
    private String teacher_name;


    public void setStudent_name(String student_name) {
        this.student_name = student_name;
    }



    public void setL_Code(int l_Code) {
        this.l_Code = l_Code;
    }

    private String l_Name;

    public int getAttendance() {
        return attendance;
    }

    public  void setAttendance(int attendance) {
        this.attendance = attendance;
    }



    public void setL_Name(String l_Name) {
        this.l_Name = l_Name;
    }

    private int attendance;
public Teacher_Student(){
    this.selected = new CheckBox();
}

    public Teacher_Student(int student_ID,String student_name,int teacher_Code,String teacher_name,int l_Code ,String l_Name , int attendance) {
        this.student_ID=student_ID;
        this.student_name = student_name;
        this.teacher_Code=teacher_Code;
        this.teacher_name=teacher_name;
        this.l_Code=l_Code;
        this.l_Name=l_Name;
        this.attendance=attendance;
        this.selected = new CheckBox();
    }

    public void setStudent_ID(int student_ID){
        this.student_ID=student_ID;

    }


    @Override
    public String toString() {
        return student_ID+","+student_name+","+teacher_Code+","+teacher_name+","+l_Code+","+l_Name+","+attendance;
    }
}
