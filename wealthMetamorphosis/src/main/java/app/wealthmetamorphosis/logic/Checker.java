package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import java.util.regex.Pattern;

public class Checker {
    public boolean isInputNumber(String input) {
        return Pattern.matches("\\w+", input);
    }

    public boolean isNumberBiggerThenZero(int input) {
        return input > 0;
    }

    public boolean areEnoughStockToBeSold(String stockSymbol, int input) {
        double ownedStockShares = UserSingleton.getInstance().getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockSymbol))
                .mapToDouble(Order::getStockShares)
                .sum();
        return ownedStockShares > input;
    }
}