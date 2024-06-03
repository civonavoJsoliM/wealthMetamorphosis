package app.wealthmetamorphosis.data;

import java.time.LocalDateTime;
import java.util.List;

public class User {
    private String userId;
    private String userName;
    private String password;
    private LocalDateTime registered;
    private double balance;
    private List<Order> orders;

    public User() {
    }

    public User(String userId, String userName, String password, LocalDateTime registered) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.registered = registered;
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

    public String getUserName() {
        return userName;
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