package app.wealthmetamorphosis.data;

import java.time.LocalDateTime;

public class Order {
    private final String orderId;
    private final String stockSymbol;
    private final double stockPrice;
    private double stockShares;
    private final OrderType orderType;
    private final LocalDateTime orderTimeStamp;
    private final String userId;

    public Order(String orderId, String stockSymbol, double stockPrice, double stockShares, OrderType orderType, LocalDateTime orderTimeStamp, String userId) {
        this.orderId = orderId;
        this.stockSymbol = stockSymbol;
        this.stockPrice = stockPrice;
        this.stockShares = stockShares;
        this.orderType = orderType;
        this.orderTimeStamp = orderTimeStamp;
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public LocalDateTime getOrderTimeStamp() {
        return orderTimeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public double getStockPrice() {
        return stockPrice;
    }

    public double getStockShares() {
        return stockShares;
    }
}