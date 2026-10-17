package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

public class EditTeacherStudentController {
    @FXML
    private AnchorPane editPane;
    @FXML
    private TextField id;
    @FXML
    private TextField lecture;
    @FXML
    private TextField attendance;
    @FXML
    private Button saveButton;

    private DataModel.Teacher_StudentDataModel teacherStudentDataModel = new DataModel.Teacher_StudentDataModel();
    private int studentID;
    Navigation navigation = new Navigation();
    private String lectureName;

    public void check() {
        String studentIDText = id.getText();
        lectureName = lecture.getText();

        if (!studentIDText.isEmpty() && !lectureName.isEmpty()) {
            try {
                studentID = Integer.parseInt(studentIDText);
                int attendanceValue = teacherStudentDataModel.getAttendanceByIdAndLecture(studentID, lectureName);

                if (attendanceValue != -1) {
                    attendance.setText(String.valueOf(attendanceValue));
                    addAttendanceChangeListener();
                } else {
                    attendance.setText("Attendance not found");
                }
            } catch (NumberFormatException e) {
                attendance.setText("Invalid ID format");
            }
        } else {
            attendance.setText("Invalid input");
        }
    }

    private void addAttendanceChangeListener() {
        attendance.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                try {
                    int newAttendance = Integer.parseInt(newValue);
                    teacherStudentDataModel.updateAttendanceByIdAndLecture(studentID, lectureName, newAttendance);
                } catch (NumberFormatException e) {
                    attendance.setText(oldValue); // Revert to the previous valid value
                }
            }
        });
    }

    public void save() {
        if (studentID != 0 && lectureName != null && !lectureName.isEmpty()) {
            try {
                int newAttendance = Integer.parseInt(attendance.getText().trim());
                teacherStudentDataModel.updateAttendanceByIdAndLecture(studentID, lectureName, newAttendance);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Attendance record updated successfully.");
                alert.showAndWait();
            } catch (NumberFormatException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid attendance number.");
                alert.showAndWait();
            }
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setHeaderText("Validation Error");
            alert.setContentText("Please find a valid student record first.");
            alert.showAndWait();
        }
    }
    public void back(){
        navigation.navigateTo(editPane,navigation.TEACHER__STUDENT_FXML);

    }

}
