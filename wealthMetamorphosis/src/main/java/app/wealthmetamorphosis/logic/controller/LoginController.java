package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.verifier.Checker;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.message.Message;
import app.wealthmetamorphosis.logic.message.login.LoginPasswordMessage;
import app.wealthmetamorphosis.logic.message.login.LoginUsernameMessage;
import app.wealthmetamorphosis.logic.service.OrderService;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import app.wealthmetamorphosis.logic.service.UserService;
import app.wealthmetamorphosis.logic.verifier.Validator;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class LoginController {

    @FXML
    private TextField usernameTextField;
    @FXML
    private Label invalidUsernameLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label invalidPasswordLabel;
    @FXML
    private VBox progressVBox;
    @FXML
    private BorderPane loginBorderPane;

    private Stage loginStage;
    private Checker checker;
    private DBReader<Order> orderDBReader;
    private List<User> users;
    private Validator validator;

    @FXML
    void initialize() {
        getStage();
        OwnedStockService ownedStockService = new OwnedStockService();
        checker = new Checker(ownedStockService);
        ResultSetToList<User> userRSTL = new UserService();
        DBReader<User> userDBReader = new DBReader<>(userRSTL);
        ResultSetToList<Order> orderRSTL = new OrderService();
        orderDBReader = new DBReader<>(orderRSTL);
        users = userDBReader.readFromDB("SELECT * FROM users");
        validator = new Validator();
    }

    private void getStage() {
        loginStage = new Stage();
        loginStage.setResizable(false);
    }

    @FXML
    void onSignInButtonClicked() {
        loginBorderPane.setOpacity(0.93);
        progressVBox.toFront();

        PauseTransition transition = new PauseTransition(Duration.seconds(2));
        transition.setOnFinished(event -> {

            if (isLoginSuccessful()) {

                setUser();
                loginStage.close();

            } else {

                getMessages();
                loginBorderPane.setOpacity(1.0);
                loginBorderPane.toFront();

            }

        });
        transition.playFromStart();
    }

    private void setUser() {
        User currentUser = users.stream().filter(user -> user.getUsername().equals(usernameTextField.getText())).findFirst().get();
        List<Order> orders = orderDBReader.readFromDB("SELECT * FROM orders WHERE user_id = '" + currentUser.getUserId() +
                "' ORDER BY order_timeStamp");
        currentUser.setOrders(orders);
        UserSingleton.setUser(currentUser);
    }

    private void getMessages() {
        Message loginUsernameMessage = new LoginUsernameMessage(usernameTextField, invalidUsernameLabel, doesUserExistInDB());
        Message loginPasswordMessage = new LoginPasswordMessage(passwordField, invalidPasswordLabel, isPasswordCorrect());

        List<Message> messages = new ArrayList<>(List.of(loginUsernameMessage, loginPasswordMessage));
        for (Message message : messages) {
            message.show();
        }
    }

    private boolean isLoginSuccessful() {
        return isUsernameTextFieldEmpty() &&
                isPasswordTextFieldEmpty() &&
                doesUserExistInDB() &&
                isPasswordCorrect();
    }

    private boolean isUsernameTextFieldEmpty() {
        return checker.isTextFieldNotEmpty(usernameTextField);
    }

    private boolean isPasswordTextFieldEmpty() {
        return checker.isTextFieldNotEmpty(passwordField);
    }

    private boolean doesUserExistInDB() {
        return checker.doesUserExistInDB(users, usernameTextField.getText());
    }

    private boolean isPasswordCorrect() {
        String hashedPassword = DigestUtils.sha256Hex(passwordField.getText());
        return checker.isPasswordCorrect(users, usernameTextField.getText(), hashedPassword);
    }

    @FXML
    void onCreateAccountClicked() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/app/wealthmetamorphosis/view/create-account-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        CreateAccountController controller = fxmlLoader.getController();
        controller.setCreateAccountStage(loginStage);
        loginStage.setScene(scene);
        loginStage.show();
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

    public Stage getLoginStage() {
        return loginStage;
    }
}