package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import javafx.scene.control.TextField;

import java.util.List;

public class Checker {
    private final OwnedStockService ownedStockService;

    public Checker(OwnedStockService ownedStockService) {
        this.ownedStockService = ownedStockService;
    }

    public boolean isNumberBiggerThenZero(TextField textField) {
        return isTextFieldNotEmpty(textField) && Double.parseDouble(textField.getText()) > 0;
    }

    public boolean areEnoughStockSharesToBeSold(User user, String stockSymbol, double input) {
        double ownedStockShares = ownedStockService.getSharesFromCertainStock(user, stockSymbol);
        return ownedStockShares >= input;
    }

    public boolean isBalanceEnough(double price) {
        return UserSingleton.getInstance().getBalance() > price;
    }

    public boolean isTextFieldNotEmpty(TextField textField) {
        return !textField.getText().isEmpty();
    }

    public boolean doesUserExistInDB(List<User> users, String username) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    public boolean isPasswordCorrect(List<User> users, String username, String hashedPassword) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .anyMatch(user -> user.getPassword().equals(hashedPassword));
    }
    public boolean doPasswordsMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}