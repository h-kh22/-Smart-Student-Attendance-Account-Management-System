package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class TeacherMainController {
    @FXML
    AnchorPane teacherMain;
    @FXML
    private Label username;
    Navigation navigation =new Navigation();
   private DataModel.Teacher_StudentDataModel teacherStudentDataModel=new DataModel.Teacher_StudentDataModel();
    @FXML
    public void initialize() {
        username.setText(LoginController.loggedInUsername);

    }

public void onLogOut(){
        navigation.navigateTo(teacherMain, navigation.LOGIN_FXML);
}
public void onStudent(){

    navigation.navigateTo(teacherMain,navigation.TEACHER__STUDENT_FXML);
}
public void onAttendance(){
        navigation.navigateTo(teacherMain,navigation.ATTENDANCE_FXML);
}
public void report(){
        navigation.navigateTo(teacherMain,navigation.REPORT_FXML);
}
}
