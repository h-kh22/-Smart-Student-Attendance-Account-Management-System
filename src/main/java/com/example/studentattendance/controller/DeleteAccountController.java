//package com.example.studentattendance.controller.unused;
//
//import com.example.studentattendance.Navigation;
//import com.example.studentattendance.models.Account;
//import com.example.studentattendance.DataModel.AccountDataModel;
//import javafx.fxml.FXML;
//import javafx.scene.control.Alert;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.VBox;
//
//public class DeleteAccountController {
//    @FXML
//    private VBox deletePane;
//    @FXML
//    private TextField username;
//
//    private AccountDataModel model;
//    private Navigation navigation;
//
//    public DeleteAccountController() {
//        model = new AccountDataModel();
//        navigation = new Navigation();
//    }
//
//    public void onDelete() {
//        String enteredUsername = username.getText();
//        Account accountToDelete = model.deleteAccountByUsername(enteredUsername);
//
//        if (accountToDelete != null) {
//            model.saveAccounts();
//            showDeleteSuccessAlert(accountToDelete.getUsername());
//            clearFields();
//        } else {
//            showDeleteErrorAlert();
//        }
//    }
//
//    public void onBack() {
//        navigation.navigateTo(deletePane, navigation.ACCOUNTS_FXML);
//    }
//
//    private void showDeleteSuccessAlert(String deletedUsername) {
//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setHeaderText("Student deleted successfully");
//        alert.setTitle("Delete");
//        alert.setContentText("The student with username '" + deletedUsername + "' has been deleted.");
//
//        alert.showAndWait();
//    }
//
//    private void showDeleteErrorAlert() {
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setHeaderText("Student not found");
//        alert.setTitle("Delete");
//        alert.setContentText("The student with the entered username was not found.");
//
//        alert.showAndWait();
//    }
//
//    private void clearFields() {
//        username.clear();
//    }
//}
