package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import com.example.studentattendance.models.Lecture;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;

public class EditLectureController {
    private Lecture selectedLecture;

    @FXML
    private GridPane editGrid;

    @FXML
    private ChoiceBox<String> showTeacher;

    @FXML
    private Button editButton;

    @FXML
    private Button backButton;

    private DataModel.LectureDataModel lectureDataModel;
    private Navigation navigation;
    private DataModel.AccountDataModel accountDataModel;

    public EditLectureController() {
        lectureDataModel = new DataModel.LectureDataModel();
        navigation = new Navigation();
        accountDataModel = new DataModel.AccountDataModel();
    }

    @FXML
    public void initialize() {
            showTeacher.getItems().addAll(getTeacherUsernames());
        }


    private ArrayList<String> getTeacherUsernames() {
        ArrayList<String> teacherUsernames = new ArrayList<>();
        for (Account account : accountDataModel.getAccounts()) {
            if (account.isTeacher() || (account.getCode() >= 100 && account.getCode() < 200)) {
                teacherUsernames.add(account.getUsername());
            }
        }
        return teacherUsernames;
    }



    @FXML
    public void onSave() {
        if (selectedLecture == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No lecture selected");
            alert.setContentText("Please select a lecture to edit.");
            alert.showAndWait();
            return;
        }
        String selectedTeacherUsername = showTeacher.getValue();
        if (selectedTeacherUsername == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Validation Error");
            alert.setContentText("Please choose a teacher.");
            alert.showAndWait();
            return;
        }
        int selectedTeacherCode = accountDataModel.getCodeByUsername(selectedTeacherUsername);

        Lecture editedLecture = new Lecture(selectedLecture.getLCode(), selectedTeacherCode, selectedTeacherUsername, selectedLecture.getLName(), selectedLecture.getClassroom());
        lectureDataModel.addLecture(editedLecture);
        lectureDataModel.removeLecture(selectedLecture);
        lectureDataModel.saveLecture();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("The lecture has been edited successfully");
        alert.setTitle("Done");
        alert.showAndWait();
    }

    @FXML
    public void onBack() {
        navigation.navigateTo(editGrid, navigation.LECTURE_FXML);
    }

    public void setSelectedLecture(Lecture selectedLecture) {
        this.selectedLecture = selectedLecture;
        if (selectedLecture != null && selectedLecture.getTeacher_name() != null) {
            showTeacher.setValue(selectedLecture.getTeacher_name());
        }
    }
}
