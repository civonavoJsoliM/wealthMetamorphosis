package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.User;
import java.util.Map;
import java.util.stream.Collectors;

public class OwnedStockService {
    public Map<String, Double> getOwnedStocks(User user) {
        return user.getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(order -> {
                            if (order.getOrderType().equals(OrderType.SELL) /*&& order.getStockShares() > 0*/) {
                                return order.getStockShares() * -1;
                            }
                            return order.getStockShares();
                        })
                )).entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
