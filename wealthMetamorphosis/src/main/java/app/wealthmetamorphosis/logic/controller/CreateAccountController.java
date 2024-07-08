package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.verifier.Checker;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.message.creatingAccount.CreatingAccountConfirmedPasswordMessage;
import app.wealthmetamorphosis.logic.message.creatingAccount.CreatingAccountPasswordMessage;
import app.wealthmetamorphosis.logic.message.creatingAccount.CreatingAccountUsernameMessage;
import app.wealthmetamorphosis.logic.message.Message;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import app.wealthmetamorphosis.logic.service.UserService;
import app.wealthmetamorphosis.logic.verifier.Validator;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public class CreateAccountController {

    @FXML
    private Label invalidPasswordLabel;
    @FXML
    private Label invalidUsernameLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label invalidConfirmPasswordLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private VBox progressVBox;
    @FXML
    private BorderPane createAccountBorderPane;

    private Stage createAccountStage;
    private DBInserter dbInserter;
    private DBReader<User> dbReader;
    private Checker checker;
    private Validator validator;

    @FXML
    void initialize() {
        OwnedStockService ownedStockService = new OwnedStockService();
        checker = new Checker(ownedStockService);
        dbInserter = new DBInserter();
        ResultSetToList<User> rstl = new UserService();
        dbReader = new DBReader<>(rstl);
        validator = new Validator();
    }

    @FXML
    void onCreateClicked() {
        createAccountBorderPane.setOpacity(0.93);
        progressVBox.toFront();

        PauseTransition transition = new PauseTransition(Duration.seconds(2));
        transition.setOnFinished(event -> {
            if (areTextFieldsNotEmpty() && isUsernameAvailable() && doPasswordsMatch()) {

                String hashedPassword = DigestUtils.sha256Hex(passwordField.getText());
                User newUser = new User(UUID.randomUUID().toString(), usernameTextField.getText(), hashedPassword, LocalDateTime.now(), 100000);
                dbInserter.insertIntoUsers(newUser);
                newUser.setOrders(new ArrayList<>());
                UserSingleton.setUser(newUser);
                createAccountStage.close();

            } else {

                getMessages();
                createAccountBorderPane.setOpacity(1.0);
                createAccountBorderPane.toFront();
            }
        });
        transition.playFromStart();
    }

    private void getMessages() {
        Message invalidUsernameMessage = new CreatingAccountUsernameMessage(usernameTextField, invalidUsernameLabel, isUsernameAvailable());
        Message invalidPasswordMessage = new CreatingAccountPasswordMessage(passwordField, invalidPasswordLabel);
        Message invalidConfirmedPasswordMessage = new CreatingAccountConfirmedPasswordMessage(confirmPasswordField,
                invalidConfirmPasswordLabel, doPasswordsMatch());

        List<Message> messages = new ArrayList<>(List.of(invalidUsernameMessage, invalidPasswordMessage, invalidConfirmedPasswordMessage));
        for (Message message : messages) {
            message.show();
        }
    }

    private boolean doPasswordsMatch() {
        return checker.doPasswordsMatch(passwordField.getText(), confirmPasswordField.getText());
    }

    private boolean isUsernameAvailable() {
        List<User> users = dbReader.readFromDB("SELECT * FROM users");
        return !checker.doesUserExistInDB(users, usernameTextField.getText());
    }

    private boolean areTextFieldsNotEmpty() {
        return doesUsernameTextFieldContainText() &&
                doesPasswordFieldContainText() &&
                doesConfirmPasswordFieldContainText();
    }

    private boolean doesUsernameTextFieldContainText() {
        return !usernameTextField.getText().isEmpty() || !usernameTextField.getText().isBlank();
    }

    private boolean doesPasswordFieldContainText() {
        return !passwordField.getText().isEmpty() || !passwordField.getText().isBlank();
    }

    private boolean doesConfirmPasswordFieldContainText() {
        return !confirmPasswordField.getText().isEmpty() || !confirmPasswordField.getText().isBlank();
    }

    @FXML
    void enteringUsername() {
        UnaryOperator<TextFormatter.Change> unaryOperator = validator.validateInput();
        TextFormatter<String> formatter = new TextFormatter<>(unaryOperator);
        usernameTextField.setTextFormatter(formatter);
    }

    @FXML
    void enteringPassword() {
        UnaryOperator<TextFormatter.Change> unaryOperator = validator.validateInput();
        TextFormatter<String> formatter = new TextFormatter<>(unaryOperator);
        passwordField.setTextFormatter(formatter);
    }

    @FXML
    void enteringConfirmPassword() {
        UnaryOperator<TextFormatter.Change> unaryOperator = validator.validateInput();
        TextFormatter<String> formatter = new TextFormatter<>(unaryOperator);
        confirmPasswordField.setTextFormatter(formatter);
    }
    public void setCreateAccountStage(Stage createAccountStage) {
        this.createAccountStage = createAccountStage;
    }
}