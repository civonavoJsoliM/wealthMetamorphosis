package app.wealthmetamorphosis.logic.orderToPlace;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.util.UUID;

public class BuyOrderToPlace implements OrderToPlace {
    private final TextField textField;
    private final Label priceLabel;
    private final Label stockLabel;

    public BuyOrderToPlace(TextField textField, Label priceLabel, Label stockLabel) {
        this.textField = textField;
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
    }

    @Override
    public void place(OrderType orderType) {
        if (orderType.equals(OrderType.BUY)) {
            double price = getPrice();
            double shares = Double.parseDouble(textField.getText());
            setNewBalance((price * shares));
            Order newOrder = getNewOrder(shares, price);
            UserSingleton.getInstance().getOrders().add(newOrder);
        }
    }

    private double getPrice() {
        return Double.parseDouble(priceLabel.getText().substring(0, priceLabel.getText().length() - 1));
    }

    private Order getNewOrder(double shares, double price) {
        return new Order(UUID.randomUUID().toString(), stockLabel.getText(), price,
                shares, OrderType.BUY, LocalDateTime.now(), UserSingleton.getInstance().getUserId());
    }

    private void setNewBalance(double value) {
        UserSingleton.getInstance().setBalance(UserSingleton.getInstance().getBalance() - value);
    }
}