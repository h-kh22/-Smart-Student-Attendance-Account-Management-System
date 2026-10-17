package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Lecture;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;

public class LectureController {

    @FXML
    private AnchorPane lecturePane;

    @FXML
    private TableView<Lecture> table;

    @FXML
    private TableColumn<Lecture, Integer> codeColumn;
    @FXML
    private TableColumn<Lecture, Integer> teacherCode;
    @FXML
    private TableColumn<Lecture, String> TeacherName;
    @FXML
    private TableColumn<Lecture, String> Lecture;
    @FXML
    private TableColumn<Lecture, String> Classroom;

    private Navigation navigation;
    private DataModel.LectureDataModel lectureDataModel;

    public LectureController() {
        navigation = new Navigation();
        lectureDataModel = new DataModel.LectureDataModel();
    }

    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("lCode"));
       teacherCode.setCellValueFactory(new PropertyValueFactory<>("teacher_Code"));
        TeacherName.setCellValueFactory(new PropertyValueFactory<>("teacher_name"));
        Lecture.setCellValueFactory(new PropertyValueFactory<>("lName"));
        Classroom.setCellValueFactory(new PropertyValueFactory<>("classroom"));






        Lecture.setCellFactory(TextFieldTableCell.forTableColumn());
        Lecture.setOnEditCommit(cellEditEvent -> {
            Lecture lecture = cellEditEvent.getRowValue();
            lecture.setLName(cellEditEvent.getNewValue());
            // Update the data model or perform any other necessary actions
        });

        Classroom.setCellFactory(TextFieldTableCell.forTableColumn());
        Classroom.setOnEditCommit(cellEditEvent -> {
            Lecture lecture = cellEditEvent.getRowValue();
            lecture.setClassroom(cellEditEvent.getNewValue());
            // Update the data model or perform any other necessary actions
        });

        table.setEditable(true);
        table.setItems(FXCollections.observableArrayList(lectureDataModel.getLectures()));
        table.refresh();
    }

    @FXML
    public void add() {
        navigation.navigateTo(lecturePane, navigation.ADD_LECTURE_FXML);
    }

    @FXML
    public void delete() {
        Lecture selectedLecture = table.getSelectionModel().getSelectedItem();
        if (selectedLecture != null) {
            lectureDataModel.removeLecture(selectedLecture);
            table.getItems().remove(selectedLecture);
        }
    }

    @FXML
    public void back() {
        navigation.navigateTo(lecturePane, navigation.ADMIN_FXML);
    }

    @FXML
    public void edit() {
        Lecture selectedLecture = table.getSelectionModel().getSelectedItem();
        if (selectedLecture != null) {
            navigation.selectLectureNavigateTo(lecturePane, navigation.EDIT_LECTURE_FXML, selectedLecture);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Account Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select an account to edit.");
            alert.showAndWait();
        }
    }


}
