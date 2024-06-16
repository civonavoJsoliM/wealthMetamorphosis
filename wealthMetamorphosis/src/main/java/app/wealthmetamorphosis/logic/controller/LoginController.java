package app.wealthmetamorphosis.logic.controller;

import app.wealthmetamorphosis.Main;
import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.Checker;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBReader;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import app.wealthmetamorphosis.logic.service.OrderService;
import app.wealthmetamorphosis.logic.service.UserService;
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
import java.util.List;

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
    private Label createAccountLabel;
    @FXML
    private Button signInButton;
    @FXML
    private VBox progressVBox;
    @FXML
    private BorderPane loginBorderPane;

    private Stage loginStage;
    private Checker checker;
    private DBInserter dbInserter;
    private DBReader<User> userDBReader;
    private ResultSetToList<User> userRSTL;
    private DBReader<Order> orderDBReader;
    private ResultSetToList<Order> orderRSTL;

    @FXML
    void initialize() {
        loginStage = new Stage();
        checker = new Checker();
        dbInserter = new DBInserter();
        userRSTL = new UserService();
        userDBReader = new DBReader<>(userRSTL);
        orderRSTL = new OrderService();
        orderDBReader = new DBReader<>(orderRSTL);
    }

    @FXML
    void onSignInButtonClicked() {
        List<User> users = userDBReader.readFromDB("SELECT * FROM users");
        loginBorderPane.setOpacity(0.93);
        progressVBox.toFront();
        PauseTransition transition = new PauseTransition(Duration.seconds(1));
        transition.setOnFinished(event -> {
            if (isLoginSuccessful(users)) {
                invalidUsernameLabel.setVisible(false);
                invalidPasswordLabel.setVisible(false);

                User currentUser = users.stream().filter(user -> user.getUserName().equals(usernameTextField.getText())).findFirst().get();

                List<Order> orders = orderDBReader.readFromDB("SELECT * FROM orders WHERE user_id = '" + currentUser.getUserId() + "' ORDER BY order_timeStamp");
                currentUser.setOrders(orders);
                UserSingleton.setUser(currentUser);

                loginStage.close();
            } else {
                usernameTextField.clear();
                invalidUsernameLabel.setVisible(true);

                passwordField.clear();
                invalidPasswordLabel.setVisible(true);
                loginBorderPane.setOpacity(1.0);
                loginBorderPane.toFront();
            }
        });
        transition.playFromStart();
    }

    private boolean isLoginSuccessful(List<User> users) {
        String hashedPassword = DigestUtils.sha256Hex(passwordField.getText());
        return checker.isTextFieldNotEmpty(usernameTextField) &&
                checker.isTextFieldNotEmpty(passwordField) &&
                checker.doesUserExistInDB(users, usernameTextField) &&
                checker.isPasswordValidForUser(users, usernameTextField, hashedPassword);
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

    public Stage getLoginStage() {
        return loginStage;
    }
}