package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Teacher_Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TakeAttendanceController {
    @FXML
    private AnchorPane takePane;

    @FXML
    private TableView<Teacher_Student> tableView;
    @FXML
    private TableColumn<Teacher_Student, Integer> studentId;
    @FXML
    private TableColumn<Teacher_Student, String> studentName;
    @FXML
    private TableColumn<Teacher_Student, CheckBox> select;
    @FXML
    private TextField id;

    private ObservableList<Teacher_Student> teacherStudentList;
    private FilteredList<Teacher_Student> filteredData;

    private final DataModel.Teacher_StudentDataModel teacher_studentDataModel = new DataModel.Teacher_StudentDataModel();
    private final Navigation navigation = new Navigation();

    public void initialize() {
        String lectureName = AttendanceController.selectedLecture;
        teacherStudentList = FXCollections.observableArrayList(
                teacher_studentDataModel.getTeacher_StudentsByLectureName(lectureName));

        studentId.setCellValueFactory(new PropertyValueFactory<>("student_ID"));
        studentName.setCellValueFactory(new PropertyValueFactory<>("student_name"));
        select.setCellValueFactory(new PropertyValueFactory<>("selected"));

        filteredData = new FilteredList<>(teacherStudentList, p -> true);

        // Filter listener
        id.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                filteredData.setPredicate(student -> true);
            } else {
                String filter = newValue.trim().toLowerCase();
                if (filter.matches("\\d+")) {
                    filteredData.setPredicate(student -> String.valueOf(student.getStudent_ID()).contains(filter));
                } else {
                    filteredData.setPredicate(student ->
                            student.getStudent_name() != null && student.getStudent_name().toLowerCase().contains(filter));
                }
            }
        });

        tableView.setItems(filteredData);
    }

    @FXML
    public void onBack() {
        int presentCount = 0;
        int totalCount = teacherStudentList.size();

        for (Teacher_Student student : teacherStudentList) {
            if (student.getSelected() != null && student.getSelected().isSelected()) {
                teacher_studentDataModel.takeAttendance(student.getStudent_ID(), AttendanceController.selectedLecture);
                presentCount++;
            }
        }

        // Persist immediately to teacher_Student.csv
        teacher_studentDataModel.saveTeacher_Student();

        // Reset checkboxes
        for (Teacher_Student student : teacherStudentList) {
            if (student.getSelected() != null) {
                student.getSelected().setSelected(false);
            }
        }

        int absentCount = totalCount - presentCount;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(LanguageManager.get("takeattendance.msg.saved_title"));
        alert.setHeaderText(null);
        alert.setContentText(String.format(
                LanguageManager.isArabic()
                        ? "تم تسجيل الحضور بنجاح لمحاضرة: %s\nالطلاب الحاضرون: %d\nالطلاب الغائبون: %d"
                        : "Attendance session recorded for '%s'.\nPresent: %d\nAbsent: %d",
                AttendanceController.selectedLecture != null ? AttendanceController.selectedLecture : "",
                presentCount, absentCount));
        alert.showAndWait();

        navigation.navigateTo(takePane, navigation.ATTENDANCE_FXML);
    }

    @FXML
    public void onSave() {
        onBack();
    }

    @FXML
    public void onCancel() {
        navigation.navigateTo(takePane, navigation.ATTENDANCE_FXML);
    }

    @FXML
    public void selectAll() {
        for (Teacher_Student ts : teacherStudentList) {
            if (ts.getSelected() != null) {
                ts.getSelected().setSelected(true);
            }
        }
    }

    @FXML
    public void deselectAll() {
        for (Teacher_Student ts : teacherStudentList) {
            if (ts.getSelected() != null) {
                ts.getSelected().setSelected(false);
            }
        }
    }

    @FXML
    public void excelPath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Attendance Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx, *.xls)", "*.xlsx", "*.xls"));

        Stage stage = (Stage) takePane.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try (FileInputStream fis = new FileInputStream(selectedFile);
                 Workbook workbook = WorkbookFactory.create(fis)) {
                Sheet sheet = workbook.getSheetAt(0);

                int matched = 0;
                for (Row row : sheet) {
                    int studentID = getStudentIDFromRow(row);
                    if (studentID <= 0) continue;
                    for (Teacher_Student student : teacherStudentList) {
                        if (student.getStudent_ID() == studentID) {
                            if (student.getSelected() != null) {
                                student.getSelected().setSelected(true);
                                matched++;
                            }
                            break;
                        }
                    }
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Excel Import");
                alert.setHeaderText(null);
                alert.setContentText(LanguageManager.isArabic()
                        ? "تمت مطابقة وتحديد " + matched + " طالب من ملف Excel."
                        : "Matched and selected " + matched + " student(s) from Excel file.");
                alert.showAndWait();

            } catch (IOException e) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Excel Import Failed");
                error.setContentText(e.getMessage());
                error.showAndWait();
            }
        }
    }

    private int getStudentIDFromRow(Row row) {
        if (row == null) return -1;
        Cell cell = row.getCell(0);
        if (cell != null) {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }
}
