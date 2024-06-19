package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import javafx.scene.control.TextField;

import java.util.List;

public class Checker {
    public boolean isNumberBiggerThenZero(TextField textField) {
        return isTextFieldNotEmpty(textField) && Double.parseDouble(textField.getText()) > 0;
    }

    public boolean areEnoughStockSharesToBeSold(String stockSymbol, double input) {
        double ownedStockShares = UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockSymbol))
                .mapToDouble(Order::getStockShares)
                .sum();
        return ownedStockShares >= input;
    }

    public boolean isBalanceEnough(double price) {
        return UserSingleton.getInstance().getBalance() > price;
    }

    public boolean isTextFieldNotEmpty(TextField textField) {
        return !textField.getText().isEmpty();
    }

    public boolean doesUserExistInDB(List<User> users, TextField textField) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(textField.getText()));
    }

    public boolean isPasswordValidForUser(List<User> users, TextField usernameTextField, String hashedPassword) {
        return users.stream()
                .filter(user -> user.getUsername().equals(usernameTextField.getText()))
                .anyMatch(user -> user.getPassword().equals(hashedPassword));
    }

    public boolean isNumberValid(TextField textField) {
        return textField.getText().matches("\\d+|\\d+\\.\\d+");
    }
}