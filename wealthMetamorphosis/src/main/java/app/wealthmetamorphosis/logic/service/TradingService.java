package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.*;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.db.DBInserter;
import app.wealthmetamorphosis.logic.db.DBUpdater;
import app.wealthmetamorphosis.logic.orderToPlace.BuyOrderToPlace;
import app.wealthmetamorphosis.logic.orderToPlace.OrderToPlace;
import app.wealthmetamorphosis.logic.orderToPlace.SellAllOrderToPlace;
import app.wealthmetamorphosis.logic.orderToPlace.SellOrderToPlace;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;

public class TradingService {
    private final Label priceLabel;
    private final Label stockLabel;
    private final TextField buySharesTextField;
    private final TextField sellSharesTextField;
    private final OwnedStockService ownedStockService;

    public TradingService(Label priceLabel, Label stockLabel, TextField buySharesTextField, TextField sellSharesTextField, OwnedStockService ownedStockService) {
        this.priceLabel = priceLabel;
        this.stockLabel = stockLabel;
        this.buySharesTextField = buySharesTextField;
        this.sellSharesTextField = sellSharesTextField;
        this.ownedStockService = ownedStockService;
    }

    public void placeOrder(OrderType orderType) {

        place(orderType);

        insertIntoDB();
    }

    private void insertIntoDB() {
        DBInserter inserter = new DBInserter();
        Order newOrder = UserSingleton.getInstance().getOrders().getLast();
        inserter.insertIntoOrders(newOrder);

        DBUpdater dbUpdater = new DBUpdater();
        dbUpdater.updateBalance();
    }

    private void place(OrderType orderType) {
        OrderToPlace buyOrderToPlace = new BuyOrderToPlace(buySharesTextField, priceLabel, stockLabel);
        OrderToPlace sellOrderToPlace = new SellOrderToPlace(sellSharesTextField, priceLabel, stockLabel);
        OrderToPlace sellAllOrderToPlace = new SellAllOrderToPlace(priceLabel, stockLabel, ownedStockService);
        List<OrderToPlace> ordersToPlace = new ArrayList<>(List.of(buyOrderToPlace, sellOrderToPlace, sellAllOrderToPlace));

        for (OrderToPlace orderToPlace : ordersToPlace) {
            orderToPlace.place(orderType);
        }
    }

}