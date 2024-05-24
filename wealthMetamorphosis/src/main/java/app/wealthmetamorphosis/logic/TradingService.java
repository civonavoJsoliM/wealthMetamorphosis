package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Account;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TradingService {
    private final Checker checker;
    private final Account account;
    private final Label priceLabel;
    private final Label stockLabel;
    private final TextField amountTextField;

    public TradingService(Checker checker, Account account, Label priceLabel, Label stockLabel, TextField amountTextField) {
        this.checker = checker;
        this.account = account;
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.amountTextField = amountTextField;
    }

    public void buyOrder() {
        double price = Double.parseDouble(priceLabel.getText()) * Double.parseDouble(amountTextField.getText());
        if (checker.isBalanceEnough(account, price)) {
            account.setBalance(account.getBalance() - price);
            if (account.getPortfolio().containsKey(stockLabel.getText())) {
                account.getPortfolio().replace(stockLabel.getText(), account.getPortfolio().get(stockLabel.getText()),
                        account.getPortfolio().get(stockLabel.getText()) + Double.parseDouble(amountTextField.getText()));
            } else {
                account.getPortfolio().put(stockLabel.getText(), Double.parseDouble(amountTextField.getText()));
            }
        } else {
            System.out.println("Not enough resources");
        }
    }

    public void sellOrder() {
        double price = Double.parseDouble(priceLabel.getText()) * Double.parseDouble(amountTextField.getText());
        if (checker.isStockAndAmountOfStockOwned(account, stockLabel.getText(), Double.parseDouble(amountTextField.getText()))) {
            account.setBalance(account.getBalance() + price);
            if (account.getPortfolio().get(stockLabel.getText()) != Double.parseDouble(amountTextField.getText())) {
                account.getPortfolio().replace(stockLabel.getText(), account.getPortfolio().get(stockLabel.getText()),
                        account.getPortfolio().get(stockLabel.getText()) - Double.parseDouble(amountTextField.getText()));
            } else {
                account.getPortfolio().remove(stockLabel.getText());
            }
        } else {
            System.out.println("Stock or amount not owned");
        }
    }
}
