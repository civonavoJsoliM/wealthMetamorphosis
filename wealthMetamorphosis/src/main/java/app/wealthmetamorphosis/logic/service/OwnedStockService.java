package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OwnedStockService {
    public Map<String, Double> getAllOwnedStockShares(User user) {
        return user.getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(order -> {
                            if (order.getOrderType().equals(OrderType.SELL) && order.getStockShares() > 0) {
                                return order.getStockShares() * -1;
                            }
                            return order.getStockShares();
                        })
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public double getSharesFromCertainStock(User user, String stockSymbol) {
        return user.getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockSymbol))
                .mapToDouble(order -> {
                    if (order.getOrderType().equals(OrderType.SELL) && order.getStockShares() > 0) {
                        return order.getStockShares() * -1;
                    }
                    return order.getStockShares();
                })
                .sum();
    }

    public double getInvestedInStock(User user, String stockSymbol) {
        List<Order> orders = getOrders(user, stockSymbol);

        double shares = 0;
        double investedInStock = 0;

        for (Order order : orders) {

            if (order.getOrderType().equals(OrderType.BUY)) {
                shares += order.getStockShares();
                investedInStock += order.getStockShares() * order.getStockPrice();
            } else {
                double percentage = getPercentage(investedInStock, order.getStockPrice(), shares);
                double amountToSell = order.getStockPrice() * order.getStockShares();
                investedInStock = investedInStock - (amountToSell / percentage);
                shares -= order.getStockShares();
            }
            if (shares == 0) {
                investedInStock = 0;
            }
        }
        return investedInStock;
    }

    private List<Order> getOrders(User user, String stockSymbol) {
        return user.getOrders().stream()
                .filter(order -> order.getStockSymbol().equals(stockSymbol))
                .toList();
    }

    public double getPercentage(double investedInStock, double stockPrice, double ownedShares) {
        double percentage = ((100 / investedInStock) * (stockPrice * ownedShares)) - 100;

        if (percentage < 0) {
            percentage *= -1;
        }

        percentage = (percentage + 100) / 100;
        return percentage;
    }

}