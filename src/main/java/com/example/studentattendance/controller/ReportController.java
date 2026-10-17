package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Teacher_Student;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.awt.Desktop;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ReportController {
    @FXML
    GridPane reportPane;
    Navigation navigation = new Navigation();
    @FXML
    private TextField id;
    private DataModel.Teacher_StudentDataModel teacher_studentDataModel = new DataModel.Teacher_StudentDataModel();
    DataModel.LectureDataModel lectureDataModel = new DataModel.LectureDataModel();


    public void initialize() {

    }

    public void onBack() {
        navigation.navigateTo(reportPane, navigation.TEACHER_MAIN_FXML);

    }

    public void searchStudent() {
        String input = id.getText();

        if (input.isEmpty()) {
            showAlert("Please enter a value for the ID field.");
            return;
        }

        if (isNumeric(input)) {
            // Search by ID
            int studentID = Integer.parseInt(input);
            Teacher_Student student = teacher_studentDataModel.getTeacher_StudentById(studentID);

            if (student == null) {
                showAlert("No student found with the ID: " + studentID);
            } else {
                generateStudentReport(student);
            }
        } else {
            // Search by Name
            Teacher_Student student = teacher_studentDataModel.getTeacher_StudentByName(input);

            if (student == null) {
                showAlert("No student found with the name: " + input);
            } else {
                generateStudentReport(student);
            }

        }
    }

    private void generateStudentReport(Teacher_Student student) {
        // Get lecture and attendance details for the student
        ArrayList<Teacher_Student> studentDetails = teacher_studentDataModel.getLectureAndAttendanceDetails(student);

        if (studentDetails.isEmpty()) {
            showAlert("No lecture and attendance details found for the student: " + student.getStudent_name());
        } else {
            // Generate Excel report
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Student Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Student Name");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Lecture");
            headerRow.createCell(3).setCellValue("Teacher Name");
            headerRow.createCell(4).setCellValue("Attendance");

            // Populate data rows
            int rowNum = 1;
            for (Teacher_Student studentDetail : studentDetails) {
                Row dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(0).setCellValue(student.getStudent_name());
                dataRow.createCell(1).setCellValue(student.getStudent_ID());
                dataRow.createCell(2).setCellValue(studentDetail.getL_Name());
                dataRow.createCell(3).setCellValue(lectureDataModel.getTeacherNameByLName(studentDetail.getL_Name()));
                dataRow.createCell(4).setCellValue(studentDetail.getAttendance());
            }

            // Auto-size columns for better visibility
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // Save the workbook to a file
            File reportFile = new File("StudentReport.xlsx");
            try (FileOutputStream outputStream = new FileOutputStream(reportFile)) {
                workbook.write(outputStream);
                showAlert("Student report generated and saved as " + reportFile.getAbsolutePath());
                openGeneratedFile(reportFile);
            } catch (IOException e) {
                showAlert("Could not save StudentReport.xlsx. If the file is open in Excel, please close it and try again.");
            } finally {
                try {
                    workbook.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void openGeneratedFile(File file) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN) && file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }
}
