package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.User;

public class Checker {
    public boolean isBalanceEnough(User user, double price) {
        return user.getBalance() > price;
    }

    /*public boolean isStockAndAmountOfStockOwned(User user, String stock, double amount) {
        return user.getPortfolio().entrySet().stream()
                .anyMatch(entry -> entry.getKey().equals(stock) && entry.getValue() >= amount);
    } */
}
