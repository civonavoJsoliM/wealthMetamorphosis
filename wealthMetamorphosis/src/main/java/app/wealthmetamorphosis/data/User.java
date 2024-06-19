package app.wealthmetamorphosis.data;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private String userId;
    private String username;
    private String password;
    private LocalDateTime registered;
    private double balance;
    private List<Order> orders;

    public User() {
    }

    public User(String userId, String username, String password, LocalDateTime registered, double balance) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.registered = registered;
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getRegistered() {
        return registered;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}