package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Account;

public class Checker {
    public boolean isBalanceEnough(Account account, double price) {
        return account.getBalance() > price;
    }

    public boolean isStockAndAmountOfStockOwned(Account account, String stock, double amount) {
        return account.getPortfolio().entrySet().stream()
                .anyMatch(entry -> entry.getKey().equals(stock) && entry.getValue() >= amount);
    }
}
