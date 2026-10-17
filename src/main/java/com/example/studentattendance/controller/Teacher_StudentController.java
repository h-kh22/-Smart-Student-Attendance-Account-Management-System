package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import java.util.ArrayList;

public class Teacher_StudentController {

    // ── FXML ─────────────────────────────────────────────────────────────────
    @FXML private AnchorPane teacherPane;
    @FXML private ChoiceBox<Lecture>                           lectureChoiceBox;
    @FXML private ComboBox<Student>                            studentComboBox;
    @FXML private Button                                       saveTeacher_Student;
    @FXML private TableView<Teacher_Student>                   tableView;
    @FXML private TableColumn<Teacher_Student, Integer>        ID;
    @FXML private TableColumn<Teacher_Student, String>         student_Name;
    @FXML private TableColumn<Teacher_Student, String>         teacher_Name;
    @FXML private TableColumn<Teacher_Student, String>         lecture;
    @FXML private TableColumn<Teacher_Student, Integer>        attendance;

    // ── Data models ───────────────────────────────────────────────────────────
    private final DataModel.LectureDataModel           lectureDataModel          = new DataModel.LectureDataModel();
    private final DataModel.Teacher_StudentDataModel   teacher_studentDataModel  = new DataModel.Teacher_StudentDataModel();
    private final DataModel.StudentDataModel           studentDataModel          = new DataModel.StudentDataModel();
    private final DataModel.AccountDataModel           accountDataModel          = new DataModel.AccountDataModel();
    private final Navigation                           navigation                = new Navigation();

    // ── Collections ───────────────────────────────────────────────────────────
    private ObservableList<Student>         originalStudents;
    private ObservableList<Teacher_Student> Teacher_Student;

    // ── State ─────────────────────────────────────────────────────────────────
    /** Last Student the user explicitly picked from the dropdown. */
    private static Student selectedStudent;

    /**
     * Guard: true while we write to the editor programmatically so the
     * text-change listener skips re-filtering during those writes.
     */
    private boolean suppressFilter = false;


    public Teacher_StudentController() {
        originalStudents = FXCollections.observableArrayList(studentDataModel.getStudents());
        Teacher_Student  = FXCollections.observableArrayList(
                teacher_studentDataModel.getTeacher_StudentsByTeacherName(
                        LoginController.loggedInUsername));
        if (Teacher_Student.isEmpty()
                && "admin".equalsIgnoreCase(LoginController.loggedInUsername)) {
            Teacher_Student = FXCollections.observableArrayList(
                    teacher_studentDataModel.getTeacher_Student());
        }
    }

    public void initialize() {
        // ── Table columns ────────────────────────────────────────────────────
        ID          .setCellValueFactory(new PropertyValueFactory<>("student_ID"));
        student_Name.setCellValueFactory(new PropertyValueFactory<>("student_name"));
        teacher_Name.setCellValueFactory(new PropertyValueFactory<>("teacher_name"));
        lecture     .setCellValueFactory(new PropertyValueFactory<>("l_Name"));
        attendance  .setCellValueFactory(new PropertyValueFactory<>("attendance"));
        tableView.setItems(Teacher_Student);

        // ── Lecture ChoiceBox ─────────────────────────────────────────────────
        setupLectureChoiceBox();

        // ── Student ComboBox ──────────────────────────────────────────────────
        setupStudentComboBox();

        tableView.refresh();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture ChoiceBox
    // ─────────────────────────────────────────────────────────────────────────
    private void setupLectureChoiceBox() {
        ObservableList<Lecture> lectures = FXCollections.observableArrayList(
                getFilteredLectures(LoginController.loggedInUsername));
        lectureChoiceBox.setItems(lectures);
        // Show only the lecture name in the dropdown
        lectureChoiceBox.setConverter(new StringConverter<>() {
            @Override public String toString(Lecture l) {
                return (l == null) ? "" : l.getLName();
            }
            @Override public Lecture fromString(String s) {
                return lectures.stream()
                        .filter(l -> l.getLName().equals(s))
                        .findFirst().orElse(null);
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Student ComboBox
    // ─────────────────────────────────────────────────────────────────────────
    private void setupStudentComboBox() {
        studentComboBox.setEditable(true);

        // StringConverter: Student <-> "ID - Name"
        studentComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Student s) {
                return (s == null) ? "" : s.getID() + " - " + s.getStudentName();
            }
            @Override
            public Student fromString(String text) {
                if (text == null || text.isBlank()) return null;
                // 1) Exact "ID - Name" match
                Student exact = originalStudents.stream()
                        .filter(s -> (s.getID() + " - " + s.getStudentName()).equals(text))
                        .findFirst().orElse(null);
                if (exact != null) return exact;
                // 2) Numeric ID only
                try {
                    int id = Integer.parseInt(text.trim());
                    return originalStudents.stream()
                            .filter(s -> s.getID() == id).findFirst().orElse(null);
                } catch (NumberFormatException ignored) {}
                // 3) Partial name
                String lower = text.toLowerCase();
                return originalStudents.stream()
                        .filter(s -> s.getStudentName().toLowerCase().contains(lower))
                        .findFirst().orElse(null);
            }
        });

        // Dropdown list cell
        studentComboBox.setCellFactory(param -> new ListCell<>() {
            @Override protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getID() + " - " + s.getStudentName());
            }
        });

        // Button (closed) cell
        studentComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Student s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null : s.getID() + " - " + s.getStudentName());
            }
        });

        // FilteredList for live search
        FilteredList<Student> filteredStudents = new FilteredList<>(originalStudents, p -> true);
        studentComboBox.setItems(filteredStudents);

        // Text listener — only filters when the user is typing (not programmatic writes)
        studentComboBox.getEditor().textProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (suppressFilter) return;

                    // If the current text matches the selected item's formatted text, skip
                    Student already = studentComboBox.getSelectionModel().getSelectedItem();
                    if (already != null) {
                        String fmt = already.getID() + " - " + already.getStudentName();
                        if (fmt.equals(newVal)) return;
                    }

                    filteredStudents.setPredicate(s -> {
                        if (s == null) return false;
                        if (newVal == null || newVal.isBlank()) return true;
                        String lower = newVal.toLowerCase();
                        return String.valueOf(s.getID()).contains(lower)
                                || s.getStudentName().toLowerCase().contains(lower);
                    });
                    if (!filteredStudents.isEmpty()) studentComboBox.show();
                    else                             studentComboBox.hide();
                });

        // Selection listener — captures the chosen Student and writes formatted text
        studentComboBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldItem, newItem) -> {
                    if (newItem != null) {
                        selectedStudent = newItem;
                        suppressFilter = true;
                        studentComboBox.getEditor()
                                .setText(newItem.getID() + " - " + newItem.getStudentName());
                        suppressFilter = false;
                        filteredStudents.setPredicate(p -> true); // reset filter
                    }
                });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lecture helpers
    // ─────────────────────────────────────────────────────────────────────────
    /** Returns Lecture objects assigned to the given teacher (or all if none found). */
    public ArrayList<Lecture> getFilteredLectures(String teacherName) {
        ArrayList<Lecture> result = new ArrayList<>();
        ArrayList<String>  seen   = new ArrayList<>();
        if (teacherName != null) {
            for (Lecture lec : lectureDataModel.getLectures()) {
                String t = lec.getTeacher_name();
                if (t != null && t.equalsIgnoreCase(teacherName)
                        && !seen.contains(lec.getLName())) {
                    seen.add(lec.getLName());
                    result.add(lec);
                }
            }
        }
        if (result.isEmpty()) {
            for (Lecture lec : lectureDataModel.getLectures()) {
                if (lec.getLName() != null && !seen.contains(lec.getLName())) {
                    seen.add(lec.getLName());
                    result.add(lec);
                }
            }
        }
        return result;
    }

    /** Legacy helper (returns names only). */
    public ArrayList<String> getFilteredLectureNames(String teacherName) {
        ArrayList<String> names = new ArrayList<>();
        for (Lecture lec : getFilteredLectures(teacherName)) names.add(lec.getLName());
        return names;
    }
    @FXML
    public void save() {
        enrollStudent();
    }

    /**
     * Core enrollment logic.
     * Priority for resolving Student: selection model -> static field -> StringConverter.fromString()
     * All alerts go through LanguageManager for EN/AR localization.
     */
    private boolean enrollStudent() {

        // 1. Resolve lecture (typed ChoiceBox gives the exact Lecture object)
        Lecture selectedLecture = lectureChoiceBox.getValue();

        // 2. Resolve student — 3-step priority
        Student student = studentComboBox.getSelectionModel().getSelectedItem();
        if (student == null) student = selectedStudent;
        if (student == null) {
            String raw = studentComboBox.getEditor().getText();
            if (raw != null && !raw.isBlank()) {
                student = studentComboBox.getConverter().fromString(raw);
            }
        }

        // 3. Validate lecture
        if (selectedLecture == null) {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.get("enroll.warn.title"),
                    LanguageManager.get("enroll.warn.no_lecture.header"),
                    LanguageManager.get("enroll.warn.no_lecture.body"));
            return false;
        }

        // 4. Validate student
        if (student == null) {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.get("enroll.warn.title"),
                    LanguageManager.get("enroll.warn.no_student.header"),
                    LanguageManager.get("enroll.warn.no_student.body"));
            return false;
        }

        // 5. Duplicate check
        final Student  fs = student;
        final String   ln = selectedLecture.getLName();
        boolean exists = Teacher_Student.stream()
                .anyMatch(ts -> ts.getStudent_ID() == fs.getID()
                        && ln != null && ln.equals(ts.getL_Name()));
        if (exists) {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.get("enroll.warn.title"),
                    LanguageManager.get("enroll.warn.duplicate.header"),
                    LanguageManager.get("enroll.warn.duplicate.body"));
            return false;
        }

        // 6. Persist and refresh
        try {
            int lectureCode = selectedLecture.getLCode();           // exact, no lookup needed
            int teacherCode = selectedLecture.getTeacher_Code();
            if (teacherCode <= 0)
                teacherCode = accountDataModel.getCodeByUsername(LoginController.loggedInUsername);
            if (teacherCode <= 0) teacherCode = 0;

            String teacherName = (LoginController.loggedInUsername != null)
                    ? LoginController.loggedInUsername : "Teacher";

            com.example.studentattendance.models.Teacher_Student ts =
                    new com.example.studentattendance.models.Teacher_Student(
                            student.getID(), student.getStudentName(),
                            teacherCode, teacherName,
                            lectureCode, selectedLecture.getLName(), 0);

            teacher_studentDataModel.addTeacher_Student(ts);
            Teacher_Student.add(ts);

            // Clear inputs
            suppressFilter = true;
            studentComboBox.getSelectionModel().clearSelection();
            studentComboBox.getEditor().clear();
            suppressFilter = false;
            selectedStudent = null;
            lectureChoiceBox.getSelectionModel().clearSelection();
            tableView.refresh();

            showAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.get("enroll.success.title"),
                    null,
                    LanguageManager.get("enroll.success.body") + " " + selectedLecture.getLName());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    LanguageManager.get("enroll.err.title"),
                    null,
                    LanguageManager.get("enroll.err.body"));
            return false;
        }
    }


    public void deleteTeacher_Student() {
        com.example.studentattendance.models.Teacher_Student sel =
                tableView.getSelectionModel().getSelectedItem();
        if (sel != null) {
            teacher_studentDataModel.removeTeacher_Student(sel);
            Teacher_Student.remove(sel);
            showAlert(Alert.AlertType.INFORMATION,
                    LanguageManager.get("enroll.success.title"), null,
                    LanguageManager.get("enroll.delete.success"));
        } else {
            showAlert(Alert.AlertType.WARNING,
                    LanguageManager.get("enroll.warn.title"), null,
                    LanguageManager.get("enroll.delete.no_selection"));
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void edit(){
        navigation.navigateTo(teacherPane,navigation.EDIT_TEACHER_STUDENT_FXML);
    }
    public void back(){
        try {
            teacher_studentDataModel.checkAndUpdateAttendance();
            navigation.navigateTo(teacherPane, navigation.TEACHER_MAIN_FXML);
        }catch (Exception e){
            System.out.println("All is good");
        }
    }

}
