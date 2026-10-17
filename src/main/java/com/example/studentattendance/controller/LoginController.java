package com.example.studentattendance.controller;

import com.example.studentattendance.AnimationUtils;
import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class LoginController {

    @FXML private AnchorPane root;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label modeLabel;
    @FXML private Label confirmPasswordLabel;
    @FXML private Label message;
    @FXML private Button submitButton;
    @FXML private Button langToggleBtn;

    static String loggedInUsername;

    private final Navigation navigation = new Navigation();
    private final DataModel.AccountDataModel model = new DataModel.AccountDataModel();
    private boolean setupMode;

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    @FXML
    public void initialize() {
        setupMode = !model.hasAccounts();

        // Ensure the language toggle button is in front of all background containers
        if (langToggleBtn != null) {
            langToggleBtn.toFront();
            langToggleBtn.setText(LanguageManager.getToggleLabel());
        }

        // Apply dynamic text from the active ResourceBundle
        refreshLabels();
    }

    /** Refreshes all user-visible labels using the current locale. */
    private void refreshLabels() {
        modeLabel.setText(setupMode
                ? LanguageManager.get("login.subtitle.setup")
                : LanguageManager.get("login.subtitle.signin"));

        submitButton.setText(setupMode
                ? LanguageManager.get("login.btn.create")
                : LanguageManager.get("login.btn.signin"));

        boolean show = setupMode;
        confirmPasswordLabel.setManaged(show);
        confirmPasswordLabel.setVisible(show);
        confirmPasswordField.setManaged(show);
        confirmPasswordField.setVisible(show);
    }

    // -------------------------------------------------------------------------
    // Language Toggle
    // -------------------------------------------------------------------------

    /**
     * Toggles between Arabic and English, then reloads the login view so
     * all %key bindings and RTL orientation are applied from scratch.
     */
    @FXML
    public void handleLangToggle() {
        LanguageManager.toggleLocale();
        reloadLoginView();
    }

    private void reloadLoginView() {
        try {
            java.net.URL res = Navigation.class.getResource("views/login.fxml");
            if (res == null) {
                res = getClass().getResource("/com/example/studentattendance/views/login.fxml");
            }
            Objects.requireNonNull(res, "Cannot find views/login.fxml");

            FXMLLoader loader = new FXMLLoader(res);
            loader.setResources(LanguageManager.getBundle());
            Parent nextRoot = loader.load();

            // Apply RTL/LTR orientation
            nextRoot.setNodeOrientation(
                    LanguageManager.isRTL()
                            ? NodeOrientation.RIGHT_TO_LEFT
                            : NodeOrientation.LEFT_TO_RIGHT);

            Scene scene = root.getScene();
            if (scene != null) {
                scene.setNodeOrientation(
                        LanguageManager.isRTL()
                                ? NodeOrientation.RIGHT_TO_LEFT
                                : NodeOrientation.LEFT_TO_RIGHT);
                Navigation.ensureStylesheet(scene);
                AnimationUtils.crossFadeScene(root, nextRoot, scene);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Login / Setup Submit
    // -------------------------------------------------------------------------

    @FXML
    public void handleSubmitButtonAction() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        message.setText("");

        if (username.isBlank() || password.isBlank()) {
            message.setText(LanguageManager.get("login.err.blank"));
            AnimationUtils.shake(submitButton);
            return;
        }

        try {
            if (setupMode) {
                if (!password.equals(confirmPasswordField.getText())) {
                    message.setText(LanguageManager.get("login.err.mismatch"));
                    AnimationUtils.shake(confirmPasswordField);
                    return;
                }
                model.createInitialAdmin(username, password);
                setupMode = false;
            } else if (!model.authenticate(username, password)) {
                message.setText(LanguageManager.get("login.err.invalid"));
                AnimationUtils.shake(submitButton);
                return;
            }

            loggedInUsername = username;
            Account account = model.getAccountByUsername(username);
            if (model.isAdmin(account)) {
                navigation.navigateTo(root, navigation.ADMIN_FXML);
            } else if (model.isTeacher(account)) {
                navigation.navigateTo(root, navigation.TEACHER_MAIN_FXML);
            } else {
                navigation.navigateTo(root, navigation.STUDENT_PORTAL_FXML);
            }
        } catch (UncheckedIOException e) {
            showError("Unable to save account data.", e);
        } catch (IllegalArgumentException | IllegalStateException e) {
            message.setText(e.getMessage());
            AnimationUtils.shake(submitButton);
        }
    }

    private void showError(String msg, Exception cause) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Account Setup Failed");
        alert.setHeaderText(msg);
        alert.setContentText(cause.getMessage());
        alert.showAndWait();
    }
}
