//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.studentattendance.controller;

import com.example.studentattendance.Navigation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class AdminController {
    @FXML
    private AnchorPane adminPane;

    @FXML
    private Button student;
    @FXML
    private Button account;
    @FXML
    private Button course;
    @FXML
    private Label user;
    Navigation navigation = new Navigation();
  //  Account account=new Account();
    public AdminController() {

    }

    public void initialize() {
        if (user != null && LoginController.loggedInUsername != null) {
            user.setText(LoginController.loggedInUsername);
        }
        account.setOnAction(event -> handleAccountButton());
        course.setOnAction(event -> handleCourseButton());
        student.setOnAction(event -> handleStudentButton());
    }
    public void logout(ActionEvent event){

        navigation.navigateTo(adminPane, navigation.LOGIN_FXML);
    }
    public void handleAccountButton(){
        navigation.navigateTo(adminPane,navigation.ACCOUNTS_FXML);

    }
    public void handleCourseButton(){
        navigation.navigateTo(adminPane,navigation.LECTURE_FXML);

    }
    public void handleStudentButton(){
        navigation.navigateTo(adminPane,navigation.Student_FXML);
    }
    }








