package app.wealthmetamorphosis.data;

import java.util.Map;

public class Account {
    private final String user;
    private double balance;
    private Map<String, Double> portfolio;

    public Account(String user, double balance, Map<String, Double> portfolio) {
        this.user = user;
        this.balance = balance;
        this.portfolio = portfolio;
    }

    public String getUser() {
        return user;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Map<String, Double> getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Map<String, Double> portfolio) {
        this.portfolio = portfolio;
    }
}
