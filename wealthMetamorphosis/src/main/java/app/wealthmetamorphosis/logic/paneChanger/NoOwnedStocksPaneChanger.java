package app.wealthmetamorphosis.logic.paneChanger;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import app.wealthmetamorphosis.logic.service.ProfileControllerService;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

public class NoOwnedStocksPaneChanger implements PaneChanger {
    private final VBox noStocksToDisplayVBox;
    private final Label portfolioWorthLabel;
    private final ScrollPane ordersScrollPane;
    private final VBox ordersVBox;
    private final ProfileControllerService profileControllerService;

    public NoOwnedStocksPaneChanger(VBox noStocksToDisplayVBox, Label portfolioWorthLabel, ScrollPane ordersScrollPane,
                                    VBox ordersVBox, ProfileControllerService profileControllerService) {
        this.noStocksToDisplayVBox = noStocksToDisplayVBox;
        this.portfolioWorthLabel = portfolioWorthLabel;
        this.ordersScrollPane = ordersScrollPane;
        this.ordersVBox = ordersVBox;
        this.profileControllerService = profileControllerService;
    }

    @Override
    public void change() {
        if (!isUserOwningStocks() && !UserSingleton.getInstance().getOrders().isEmpty()) {
            noStocksToDisplayVBox.toFront();
            profileControllerService.fillOrdersVBox(ordersVBox);
            ordersScrollPane.toFront();
            portfolioWorthLabel.setText("0$");
        }
    }
    private boolean isUserOwningStocks() {
        return UserSingleton.getInstance().getOrders().stream()
                .collect(Collectors.groupingBy(Order::getStockSymbol, Collectors.summingDouble(order -> {
                            if (order.getOrderType().equals(OrderType.SELL)) {
                                return order.getStockShares() * -1;
                            }
                            return order.getStockShares();
                        })
                )).entrySet().stream()
                .anyMatch(entry -> entry.getValue() > 0);
    }
}