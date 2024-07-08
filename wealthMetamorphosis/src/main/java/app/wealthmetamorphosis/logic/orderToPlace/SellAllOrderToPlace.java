package app.wealthmetamorphosis.logic.orderToPlace;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.util.UUID;

public class SellAllOrderToPlace implements OrderToPlace {
    private final Label priceLabel;
    private final Label stockLabel;
    private final OwnedStockService ownedStockService;

    public SellAllOrderToPlace(Label priceLabel, Label stockLabel, OwnedStockService ownedStockService) {
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.ownedStockService = ownedStockService;
    }

    @Override
    public void place(OrderType orderType) {
        if (orderType.equals(OrderType.SELL_ALL)) {
            double price = getPrice();
            double shares = ownedStockService.getSharesFromCertainStock(UserSingleton.getInstance(), stockLabel.getText());

            setNewBalance(price * shares);
            Order newOrder = getNewOrder(shares, price);
            UserSingleton.getInstance().getOrders().add(newOrder);
        }
    }

    private double getPrice() {
        return Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
    }

    private Order getNewOrder(double shares, double price) {
        return new Order(UUID.randomUUID().toString(), stockLabel.getText(), price,
                shares, OrderType.SELL, LocalDateTime.now(), UserSingleton.getInstance().getUserId());
    }

    private void setNewBalance(double value) {
        UserSingleton.getInstance().setBalance(UserSingleton.getInstance().getBalance() + value);
    }
}