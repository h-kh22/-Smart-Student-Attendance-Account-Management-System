package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.UncheckedIOException;

public class EditAccountController {
    @FXML
    private VBox editPane;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    private final DataModel.AccountDataModel model;
    private final Navigation navigation;
    private Account selectedAccount;

    public EditAccountController() {
        model = new DataModel.AccountDataModel();
        navigation = new Navigation();
    }

    @FXML
    public void initialize() {
        if (roleChoiceBox != null) {
            roleChoiceBox.getItems().clear();
            roleChoiceBox.getItems().addAll(
                    LanguageManager.get("role.student"),
                    LanguageManager.get("role.teacher"),
                    LanguageManager.get("role.admin")
            );
        }
    }

    @FXML
    public void onSave() {
        String newUsername = username.getText() != null ? username.getText().trim() : "";
        String newPassword = password.getText();

        if (selectedAccount == null) {
            showAlert(Alert.AlertType.ERROR, "No account is selected.");
            return;
        }

        int selectedIndex = roleChoiceBox != null ? roleChoiceBox.getSelectionModel().getSelectedIndex() : 0;
        String selectedRoleDisplay = roleChoiceBox != null ? roleChoiceBox.getValue() : null;
        String role = "STUDENT";
        if (selectedIndex == 2 || (selectedRoleDisplay != null && (selectedRoleDisplay.equalsIgnoreCase("Administrator") || selectedRoleDisplay.contains("مسؤول") || selectedRoleDisplay.equalsIgnoreCase("Admin")))) {
            role = "ADMIN";
        } else if (selectedIndex == 1 || (selectedRoleDisplay != null && (selectedRoleDisplay.toLowerCase().contains("teach") || selectedRoleDisplay.toLowerCase().contains("doctor") || selectedRoleDisplay.toLowerCase().contains("prof") || selectedRoleDisplay.contains("أستاذ") || selectedRoleDisplay.contains("دكتور")))) {
            role = "TEACHER";
        }

        try {
            selectedAccount.setRole(role);
            model.updateAccount(selectedAccount, newUsername, newPassword);
            navigation.navigateTo(editPane, navigation.ACCOUNTS_FXML);
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, e.getMessage());
        } catch (UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Unable to save account data: " + e.getMessage());
        }
    }

    @FXML
    public void onBack() {
        navigation.navigateTo(editPane, navigation.ACCOUNTS_FXML);
    }

    public void setSelectedAccount(Account selectedAccount) {
        this.selectedAccount = selectedAccount;
        username.setText(selectedAccount != null ? selectedAccount.getUsername() : "");
        password.clear();

        if (roleChoiceBox != null && selectedAccount != null) {
            if (selectedAccount.isAdmin()) {
                roleChoiceBox.getSelectionModel().select(2);
            } else if (selectedAccount.isTeacher()) {
                roleChoiceBox.getSelectionModel().select(1);
            } else {
                roleChoiceBox.getSelectionModel().select(0);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
