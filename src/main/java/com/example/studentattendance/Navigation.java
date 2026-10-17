package com.example.studentattendance;

import com.example.studentattendance.controller.EditAccountController;
import com.example.studentattendance.controller.EditLectureController;
import com.example.studentattendance.models.Account;
import com.example.studentattendance.models.Lecture;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public class Navigation {
    public final String LOGIN_FXML = "views/login.fxml";
    public final String ADMIN_FXML = "views/admin.fxml";
    public final String LECTURE_FXML = "views/lecture.fxml";
    public final String ADD_LECTURE_FXML = "views/addLecture.fxml";
    public final String EDIT_LECTURE_FXML = "views/editLecture.fxml";
    public final String TEACHER_FXML = "views/teacher_Student.fxml";
    public final String ACCOUNTS_FXML = "views/accounts.fxml";
    public final String ADD_ACCOUNTS_FXML = "views/addAccount.fxml";
    public final String EDIT_ACCOUNTS_FXML = "views/editAccount.fxml";
    public final String TEACHER_MAIN_FXML = "views/teacherMain.fxml";
    public final String Student_FXML = "views/student.fxml";
    public final String TEACHER__STUDENT_FXML = "views/teacher_Student.fxml";
    public final String ADD_STUDENT_FXML = "views/addStudent.fxml";
    public final String EDIT_TEACHER_STUDENT_FXML = "views/editTeacherStudent.fxml";
    public final String ATTENDANCE_FXML = "views/attendance.fxml";
    public final String TAKE_ATTENDANCE_FXML = "views/takeAttendance.fxml";
    public final String REPORT_FXML = "views/report.fxml";
    public final String STUDENT_PORTAL_FXML = "views/studentPortal.fxml";

    /**
     * Navigates to a new FXML view with smooth cross-fade/slide animation.
     * Automatically injects the active ResourceBundle and applies RTL orientation.
     */
    public void navigateTo(Parent rootPane, String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                System.err.println("Cannot find view resource: " + path);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            loader.setResources(LanguageManager.getBundle());
            Parent nextRoot = loader.load();

            applyOrientation(nextRoot);

            Scene scene = rootPane.getScene();
            if (scene != null) {
                ensureStylesheet(scene);
                scene.setNodeOrientation(LanguageManager.isRTL() ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
                AnimationUtils.crossFadeScene(rootPane, nextRoot, scene);
            } else {
                System.err.println("Warning: Scene was null when navigating to " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to Edit Account view inside an AnchorPane container.
     */
    public void selectAccountNavigateTo(AnchorPane pane, String fxmlFile, Account selectedAccount) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(fxmlFile), "Resource not found: " + fxmlFile));
            loader.setResources(LanguageManager.getBundle());
            Parent root = loader.load();

            EditAccountController ctrl = loader.getController();
            ctrl.setSelectedAccount(selectedAccount);
            root.setUserData(ctrl);

            applyOrientation(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            pane.getChildren().setAll(root);
            AnimationUtils.fadeInChild(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to Edit Lecture view inside an AnchorPane container.
     */
    public void selectLectureNavigateTo(AnchorPane pane, String fxmlFile, Lecture selectedLecture) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(fxmlFile), "Resource not found: " + fxmlFile));
            loader.setResources(LanguageManager.getBundle());
            Parent root = loader.load();

            EditLectureController ctrl = loader.getController();
            ctrl.setSelectedLecture(selectedLecture);
            root.setUserData(ctrl);

            applyOrientation(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            pane.getChildren().setAll(root);
            AnimationUtils.fadeInChild(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Static utilities
    // -------------------------------------------------------------------------

    /** Ensures the global stylesheet is attached to the scene. */
    public static void ensureStylesheet(Scene scene) {
        if (scene == null) return;
        URL css = Navigation.class.getResource("styles/style.css");
        if (css != null) {
            String path = css.toExternalForm();
            if (!scene.getStylesheets().contains(path)) {
                scene.getStylesheets().add(path);
            }
        }
    }

    /** Applies RTL or LTR node orientation and style classes based on the active locale. */
    public static void applyOrientation(Parent root) {
        if (root != null) {
            boolean rtl = LanguageManager.isRTL();
            NodeOrientation orientation = rtl ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
            root.setNodeOrientation(orientation);
            if (root.getScene() != null) {
                root.getScene().setNodeOrientation(orientation);
            }
            if (rtl) {
                if (!root.getStyleClass().contains("rtl-root")) {
                    root.getStyleClass().add("rtl-root");
                }
            } else {
                root.getStyleClass().remove("rtl-root");
            }
        }
    }

    /** Applies the custom application branding logo icon to the given Stage. */
    public static void applyStageIcon(Stage stage) {
        if (stage == null) return;
        try {
            URL iconUrl = Navigation.class.getResource("images/app_icon.png");
            if (iconUrl == null) {
                iconUrl = Navigation.class.getResource("/com/example/studentattendance/images/app_icon.png");
            }
            if (iconUrl != null) {
                Image appIcon = new Image(iconUrl.toExternalForm());
                stage.getIcons().clear();
                stage.getIcons().add(appIcon);
            }
        } catch (Exception e) {
            System.err.println("Could not apply application stage icon: " + e.getMessage());
        }
    }
}