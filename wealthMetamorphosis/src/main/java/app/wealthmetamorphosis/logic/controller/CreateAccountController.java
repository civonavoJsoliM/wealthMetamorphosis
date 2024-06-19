package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.Checker;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.service.UserService;
import app.wealthmetamorphosis.logic.service.Validator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateAccountController {

    @FXML
    private Label invalidPasswordLabel;
    @FXML
    private Label invalidUsernameLabel;
    @FXML
    private Button createButton;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label invalidConfirmPasswordField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField confirmPasswordField;

    private Stage createAccountStage;
    private DBInserter dbInserter;
    private ResultSetToList<User> rstl;
    private DBReader<User> dbReader;
    private Checker checker;
    private Validator validator;

    @FXML
    void initialize() {
        dbInserter = new DBInserter();
        rstl = new UserService();
        dbReader = new DBReader<>(rstl);
        checker = new Checker();
        validator = new Validator();
    }

    @FXML
    void onCreateClicked() {
        List<User> users = dbReader.readFromDB("SELECT * FROM users");
        if (areTextFieldsNotEmpty() && users.stream().map(User::getUsername).noneMatch(username -> username.equals(usernameTextField.getText())) &&
                passwordField.getText().equals(confirmPasswordField.getText())) {

            String hashedPassword = DigestUtils.sha256Hex(passwordField.getText());
            User newUser = new User(UUID.randomUUID().toString(), usernameTextField.getText(), hashedPassword, LocalDateTime.now(), 100000);
            dbInserter.insertIntoUsers(newUser);
            newUser.setOrders(new ArrayList<>());
            UserSingleton.setUser(newUser);
            createAccountStage.close();
        }
    }

    private boolean areTextFieldsNotEmpty() {
        return !usernameTextField.getText().isEmpty() &&
                !passwordField.getText().isEmpty() &&
                !confirmPasswordField.getText().isEmpty();
    }

    public void setCreateAccountStage(Stage createAccountStage) {
        this.createAccountStage = createAccountStage;
    }
}