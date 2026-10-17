package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Student;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.Optional;

public class StudentController {

    @FXML
    private BorderPane studentPane;
    @FXML
    private CheckBox filterCheckBox;

    @FXML
    private TableView<Student> tableView;

    @FXML
    private TableColumn<Student, String> idColumn;

    @FXML
    private TableColumn<Student, String> studentNameColumn;

    @FXML
    private TableColumn<Student, String> mobileNumberColumn;

    @FXML
    private TableColumn<Student, String> residenceAreaColumn;

    @FXML
    private TextField idTextField;

    @FXML
    private TextField studentNameTextField;

    @FXML
    private TextField mobileNumberTextField;

    @FXML
    private TextField residenceAreaTextField;
    @FXML
    private Button delete;

    private Navigation navigation;
    private DataModel.StudentDataModel studentDataModel;
    private ObservableList<Student> originalStudents;

    public StudentController() {
        navigation = new Navigation();
        studentDataModel = new DataModel.StudentDataModel();
        originalStudents = FXCollections.observableArrayList(studentDataModel.getStudents());
    }

    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        mobileNumberColumn.setCellValueFactory(new PropertyValueFactory<>("mobileNumber"));
        residenceAreaColumn.setCellValueFactory(new PropertyValueFactory<>("residenceArea"));

        tableView.setEditable(false);
        tableView.setItems(originalStudents);
    }

    @FXML
    public void onFilter() {
        if (filterCheckBox.isSelected()) {
            tableView.setEditable(false);
            FilteredList<Student> filteredStudents = new FilteredList<>(originalStudents);
            delete.setDisable(true);
            // Set the filtered list as the items in the TableView
            tableView.setItems(filteredStudents);

            // Add a listener to the idTextField to filter the TableView based on the entered ID
            idTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredStudents.setPredicate(student -> {
                    // If the filter text is empty, show all students
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Compare the entered ID with the student's ID
                    try {
                        int enteredId = Integer.parseInt(newValue.trim());
                        return student.getID() == enteredId;
                    } catch (NumberFormatException e) {
                        return String.valueOf(student.getID()).contains(newValue.trim());
                    }
                });
            });

            // Add a listener to the studentNameTextField to filter the TableView based on the entered student name
            studentNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredStudents.setPredicate(student -> {
                    // If the filter text is empty, show all students
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Convert both the filter text and student name to lowercase for case-insensitive comparison
                    String filterText = newValue.toLowerCase();
                    String studentName = student.getStudentName().toLowerCase();

                    // Check if the student name contains the filter text
                    return studentName.contains(filterText);
                });
            });

            // Add a listener to the mobileNumberTextField.
            // Add a listener to the mobileNumberTextField to filter the TableView based on the entered mobile number
            mobileNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredStudents.setPredicate(student -> {
                    // If the filter text is empty, show all students
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Convert both the filter text and mobile number to lowercase for case-insensitive comparison
                    String filterText = newValue.toLowerCase();
                    String mobileNumber = student.getMobileNumber().toLowerCase();

                    // Check if the mobile number contains the filter text
                    return mobileNumber.contains(filterText);
                });
            });

            // Add a listener to the residenceAreaTextField to filter the TableView based on the entered residence area
            residenceAreaTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredStudents.setPredicate(student -> {
                    // If the filter text is empty, show all students
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }

                    // Convert both the filter text and residence area to lowercase for case-insensitive comparison
                    String filterText = newValue.toLowerCase();
                    String residenceArea = student.getResidenceArea().toLowerCase();

                    // Check if the residence area contains the filter text
                    return residenceArea.contains(filterText);
                });
            });
        } else {
            tableView.setEditable(true);
            tableView.setItems(originalStudents);
            delete.setDisable(false);
        }
    }

    @FXML
    public void add() {
        navigation.navigateTo(studentPane,navigation.ADD_STUDENT_FXML);
    }


    @FXML
    public void delete() {
        Student selectedStudent = tableView.getSelectionModel().getSelectedItem();

        if (selectedStudent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to delete this student?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                originalStudents.remove(selectedStudent);
                studentDataModel.removeStudent(selectedStudent);
            }
        }
    }

    @FXML
    public void back() {
        navigation.navigateTo(studentPane,navigation.ADMIN_FXML);
    }

    private void clearFields() {
        idTextField.clear();
        studentNameTextField.clear();
        mobileNumberTextField.clear();
        residenceAreaTextField.clear();
    }
}
