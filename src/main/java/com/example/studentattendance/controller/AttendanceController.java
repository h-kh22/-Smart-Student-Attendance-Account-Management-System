package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import com.example.studentattendance.models.Lecture;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class AttendanceController {
    public static String selectedLecture;

    @FXML
    private AnchorPane attendancePane;
    @FXML
    private ChoiceBox<String> lectureChoiceBox;

    private final DataModel.LectureDataModel lectureDataModel = new DataModel.LectureDataModel();
    private final DataModel.AccountDataModel accountDataModel = new DataModel.AccountDataModel();
    private final Navigation navigation = new Navigation();

    @FXML
    public void initialize() {
        ArrayList<String> lectures = getFilteredLectureNames(LoginController.loggedInUsername);
        lectureChoiceBox.getItems().clear();
        lectureChoiceBox.getItems().addAll(lectures);
        if (!lectures.isEmpty()) {
            lectureChoiceBox.getSelectionModel().select(0);
        }
    }

    @FXML
    public void onAttendance() {
        selectedLecture = lectureChoiceBox.getSelectionModel().getSelectedItem();
        if (selectedLecture == null || selectedLecture.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(LanguageManager.get("attendance.alert.no_lecture_title"));
            alert.setContentText(LanguageManager.get("attendance.alert.no_lecture_msg"));
            alert.showAndWait();
            return;
        }
        navigation.navigateTo(attendancePane, navigation.TAKE_ATTENDANCE_FXML);
    }

    @FXML
    public void onBack() {
        Account acc = accountDataModel.getAccountByUsername(LoginController.loggedInUsername);
        if (accountDataModel.isAdmin(acc)) {
            navigation.navigateTo(attendancePane, navigation.ADMIN_FXML);
        } else {
            navigation.navigateTo(attendancePane, navigation.TEACHER_MAIN_FXML);
        }
    }

    public ArrayList<String> getFilteredLectureNames(String teacherName) {
        ArrayList<String> filteredLectureNames = new ArrayList<>();
        Account account = accountDataModel.getAccountByUsername(teacherName);
        boolean isAdmin = accountDataModel.isAdmin(account);

        for (Lecture lecture : lectureDataModel.getLectures()) {
            if (isAdmin || (lecture.getTeacher_name() != null && lecture.getTeacher_name().equalsIgnoreCase(teacherName))) {
                if (lecture.getLName() != null && !filteredLectureNames.contains(lecture.getLName())) {
                    filteredLectureNames.add(lecture.getLName());
                }
            }
        }

        // Fallback: If no lectures specifically match this teacher name, list available courses
        if (filteredLectureNames.isEmpty()) {
            for (Lecture lecture : lectureDataModel.getLectures()) {
                if (lecture.getLName() != null && !filteredLectureNames.contains(lecture.getLName())) {
                    filteredLectureNames.add(lecture.getLName());
                }
            }
        }
        return filteredLectureNames;
    }
}
