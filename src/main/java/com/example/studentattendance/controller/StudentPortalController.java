package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import com.example.studentattendance.models.Student;
import com.example.studentattendance.models.Teacher_Student;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;

public class StudentPortalController {

    @FXML
    private AnchorPane studentPortalPane;
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private Label totalCoursesLabel;
    @FXML
    private Label totalAttendanceLabel;
    @FXML
    private Label statusBadgeLabel;

    @FXML
    private TableView<Teacher_Student> attendanceTable;
    @FXML
    private TableColumn<Teacher_Student, String> lectureNameCol;
    @FXML
    private TableColumn<Teacher_Student, String> teacherNameCol;
    @FXML
    private TableColumn<Teacher_Student, Integer> lectureCodeCol;
    @FXML
    private TableColumn<Teacher_Student, Integer> attendanceCol;
    @FXML
    private TableColumn<Teacher_Student, String> statusCol;

    private final Navigation navigation = new Navigation();
    private final DataModel.Teacher_StudentDataModel teacherStudentDataModel = new DataModel.Teacher_StudentDataModel();
    private final DataModel.StudentDataModel studentDataModel = new DataModel.StudentDataModel();
    private final DataModel.AccountDataModel accountDataModel = new DataModel.AccountDataModel();

    @FXML
    public void initialize() {
        String username = LoginController.loggedInUsername;
        if (username == null || username.isBlank()) {
            username = "Student";
        }

        // Find student record if available
        Student student = studentDataModel.getStudentByName(username);
        int studentId = -1;
        String displayName = username;

        if (student != null) {
            studentId = student.getID();
            displayName = student.getStudentName();
        } else {
            // Check account code
            Account acc = accountDataModel.getAccountByUsername(username);
            if (acc != null) {
                studentId = acc.getCode();
            }
        }

        if (studentNameLabel != null) {
            studentNameLabel.setText(String.format(LanguageManager.get("studentportal.welcome"), displayName));
        }
        if (studentIdLabel != null) {
            studentIdLabel.setText(String.format(LanguageManager.get("studentportal.id"), studentId > 0 ? String.valueOf(studentId) : "N/A"));
        }

        // Setup table columns
        lectureNameCol.setCellValueFactory(new PropertyValueFactory<>("l_Name"));
        teacherNameCol.setCellValueFactory(new PropertyValueFactory<>("teacher_name"));
        lectureCodeCol.setCellValueFactory(new PropertyValueFactory<>("l_Code"));
        attendanceCol.setCellValueFactory(new PropertyValueFactory<>("attendance"));
        statusCol.setCellValueFactory(cellData -> {
            int attended = cellData.getValue().getAttendance();
            String status = attended > 0
                    ? (LanguageManager.isArabic() ? "منتظم" : "Good Standing")
                    : (LanguageManager.isArabic() ? "بدون حضور" : "No Attendance");
            return new ReadOnlyStringWrapper(status);
        });

        loadStudentAttendance(studentId, username);
    }

    private void loadStudentAttendance(int studentId, String username) {
        ArrayList<Teacher_Student> records = new ArrayList<>();
        if (studentId > 0) {
            records.addAll(teacherStudentDataModel.getTeacher_StudentsByStudentID(studentId));
        }
        if (records.isEmpty()) {
            records.addAll(teacherStudentDataModel.getTeacher_StudentsByStudentName(username));
        }

        ObservableList<Teacher_Student> list = FXCollections.observableArrayList(records);
        attendanceTable.setItems(list);

        // Compute summary metrics
        int totalCourses = list.size();
        int totalAttended = 0;
        for (Teacher_Student ts : list) {
            totalAttended += ts.getAttendance();
        }

        if (totalCoursesLabel != null) {
            totalCoursesLabel.setText(String.format(LanguageManager.get("studentportal.total_courses"), totalCourses));
        }
        if (totalAttendanceLabel != null) {
            totalAttendanceLabel.setText(String.format(LanguageManager.get("studentportal.total_attendance"), totalAttended));
        }
        if (statusBadgeLabel != null) {
            String standing = totalAttended > 0
                    ? (LanguageManager.isArabic() ? "نشط ومنتظم" : "Active & Regular")
                    : (LanguageManager.isArabic() ? "قيد المتابعة" : "New / Pending");
            statusBadgeLabel.setText(String.format(LanguageManager.get("studentportal.status"), standing));
        }
    }

    @FXML
    public void onRefresh() {
        initialize();
    }

    @FXML
    public void onLogout() {
        navigation.navigateTo(studentPortalPane, navigation.LOGIN_FXML);
    }
}
