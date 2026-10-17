package com.example.studentattendance.controller;

import com.example.studentattendance.DataModel;
import com.example.studentattendance.LanguageManager;
import com.example.studentattendance.Navigation;
import com.example.studentattendance.models.Account;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

public class AccountsController {
    @FXML
    private AnchorPane accountPane;
    @FXML
    private TableView<Account> tableView;
    @FXML
    private TableColumn<Account, Integer> codeColumn;
    @FXML
    private TableColumn<Account, String> usernameColumn;
    @FXML
    private TableColumn<Account, String> roleColumn;
    @FXML
    private TableColumn<Account, String> passwordColumn;

    private final Navigation navigation = new Navigation();
    private final DataModel.AccountDataModel model = new DataModel.AccountDataModel();

    public AccountsController() {
    }

    @FXML
    public void initialize() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (roleColumn != null) {
            roleColumn.setCellValueFactory(cellData ->
                    new ReadOnlyStringWrapper(LanguageManager.getRoleDisplay(cellData.getValue().getRole())));
        }
        passwordColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper("••••••••"));

        tableView.setItems(FXCollections.observableArrayList(model.getAccounts()));
        tableView.refresh();
    }

    @FXML
    public void add() {
        navigation.navigateTo(accountPane, navigation.ADD_ACCOUNTS_FXML);
    }

    @FXML
    public void back() {
        navigation.navigateTo(accountPane, navigation.ADMIN_FXML);
    }

    @FXML
    public void edit() {
        Account selectedAccount = tableView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            navigation.selectAccountNavigateTo(accountPane, navigation.EDIT_ACCOUNTS_FXML, selectedAccount);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(LanguageManager.get("accounts.alert.no_selection_title"));
            alert.setHeaderText(null);
            alert.setContentText(LanguageManager.get("accounts.alert.no_selection_msg"));
            alert.showAndWait();
        }
    }

    @FXML
    public void delete() {
        Account selectedAccount = tableView.getSelectionModel().getSelectedItem();
        if (selectedAccount != null) {
            model.removeAccount(selectedAccount);
            tableView.getItems().remove(selectedAccount);
            tableView.refresh();
        }
    }
}
