package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AddAccountController implements Initializable {
    @FXML
    private VBox addPane;

    private final DataModel.AccountDataModel model;
    private final Navigation navigation;

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private ChoiceBox<String> roleChoiceBox;

    public AddAccountController() {
        model = new DataModel.AccountDataModel();
        navigation = new Navigation();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (roleChoiceBox != null) {
            roleChoiceBox.getItems().clear();
            roleChoiceBox.getItems().addAll(
                    LanguageManager.get("role.student"),
                    LanguageManager.get("role.teacher"),
                    LanguageManager.get("role.admin")
            );
            // Default is Student as required
            roleChoiceBox.getSelectionModel().select(0);
        }
    }

    @FXML
    public void onAdd() {
        String accountUsername = username.getText() != null ? username.getText().trim() : "";
        String accountPassword = password.getText();
        if (accountUsername.isBlank() || accountPassword == null || accountPassword.isBlank()) {
            showAlert(Alert.AlertType.WARNING, LanguageManager.get("login.err.blank"));
            return;
        }

        int selectedIndex = roleChoiceBox != null ? roleChoiceBox.getSelectionModel().getSelectedIndex() : 0;
        String selectedRoleDisplay = roleChoiceBox != null ? roleChoiceBox.getValue() : null;
        String role = "STUDENT";
        int uniqueNumber;

        if (selectedIndex == 2 || (selectedRoleDisplay != null && (selectedRoleDisplay.equalsIgnoreCase("Administrator") || selectedRoleDisplay.contains("مسؤول") || selectedRoleDisplay.equalsIgnoreCase("Admin")))) {
            role = "ADMIN";
            uniqueNumber = model.generateUniqueCode(1, 99);
        } else if (selectedIndex == 1 || (selectedRoleDisplay != null && (selectedRoleDisplay.toLowerCase().contains("teach") || selectedRoleDisplay.toLowerCase().contains("doctor") || selectedRoleDisplay.toLowerCase().contains("prof") || selectedRoleDisplay.contains("أستاذ") || selectedRoleDisplay.contains("دكتور")))) {
            role = "TEACHER";
            uniqueNumber = model.generateUniqueCode(100, 199);
        } else {
            role = "STUDENT";
            uniqueNumber = model.generateUniqueCode(200, 99999);
        }

        try {
            Account newAccount = new Account(uniqueNumber, accountUsername, accountPassword, role);
            model.addAccount(newAccount);
            showAlert(Alert.AlertType.INFORMATION, LanguageManager.get("addaccount.msg.success"));
            clearFields();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        } catch (UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to save account data: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        navigation.navigateTo(addPane, navigation.ACCOUNTS_FXML);
    }

    private void clearFields() {
        username.clear();
        password.clear();
        if (roleChoiceBox != null) {
            roleChoiceBox.getSelectionModel().select(0);
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
