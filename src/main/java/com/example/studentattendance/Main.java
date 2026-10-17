package com.example.studentattendance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Application.launch(StudentAttendanceApp.class, args);
    }

    public static class StudentAttendanceApp extends Application {
        @Override
        public void start(Stage primaryStage) throws IOException {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("views/login.fxml"),
                            "Cannot find views/login.fxml"));
            // Inject the active ResourceBundle so %keys in login.fxml resolve correctly
            loader.setResources(LanguageManager.getBundle());
            Parent root = loader.load();

            // Apply RTL/LTR to the initial scene root
            root.setNodeOrientation(
                    LanguageManager.isRTL()
                            ? NodeOrientation.RIGHT_TO_LEFT
                            : NodeOrientation.LEFT_TO_RIGHT);

            Scene scene = new Scene(root, 900, 600);
            scene.setNodeOrientation(LanguageManager.isRTL() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            Navigation.ensureStylesheet(scene);
            Navigation.applyOrientation(root);

            // Set application window & taskbar icon
            Navigation.applyStageIcon(primaryStage);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Student Attendance System");
            primaryStage.setMinWidth(850);
            primaryStage.setMinHeight(560);
            primaryStage.show();

            // Entrance animation on startup
            AnimationUtils.animateEntrance(root);

            primaryStage.setOnCloseRequest(event -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle(LanguageManager.get("app.close.title"));
                alert.setHeaderText(LanguageManager.get("app.close.header"));

                ButtonType save      = new ButtonType(LanguageManager.get("app.close.save"),   ButtonBar.ButtonData.OK_DONE);
                ButtonType cancel    = new ButtonType(LanguageManager.get("app.close.cancel"), ButtonBar.ButtonData.FINISH);
                ButtonType doNotSave = new ButtonType(LanguageManager.get("app.close.nosave"), ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(save, cancel, doNotSave);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isEmpty() || result.get() == cancel) {
                    event.consume();
                    return;
                }

                if (result.get() == save) {
                    try {
                        new DataModel.StudentDataModel().saveStudent();
                        new DataModel.LectureDataModel().saveLecture();
                        new DataModel.Teacher_StudentDataModel().saveTeacher_Student();
                        new DataModel.AccountDataModel().saveAccounts();
                    } catch (UncheckedIOException e) {
                        event.consume();
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Could Not Save Data");
                        error.setHeaderText("The application will remain open.");
                        error.setContentText(e.getMessage());
                        error.showAndWait();
                    }
                }
            });
        }
    }
}