package app.wealthmetamorphosis.data;

import java.util.List;
import java.util.Map;

public class Account {
    private String user;
    private double balance;
    private List<Order> orders;
    private Map<String, Double> portfolio;

    public Account() {}

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

    public void setTransactions(List<Order> orders) {
        this.orders = orders;
    }

    public List<Order> getTransactions() {
        return orders;
    }
}