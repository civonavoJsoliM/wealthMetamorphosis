package app.wealthmetamorphosis.logic.paneChanger;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.parameters.OrderAvailablePaneChangerParameters;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.data.singleton.UserSingleton;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class OrdersAndStocksAvailablePaneChanger implements PaneChanger {
    private final OrderAvailablePaneChangerParameters orderAvailablePaneChangerParameters;

    public OrdersAndStocksAvailablePaneChanger(OrderAvailablePaneChangerParameters orderAvailablePaneChangerParameters) {
        this.orderAvailablePaneChangerParameters = orderAvailablePaneChangerParameters;
    }


    @Override
    public void change() {
        if (!UserSingleton.getInstance().getOrders().isEmpty() && !isUserOwningStocks()) {
            orderAvailablePaneChangerParameters.progressVBox().toFront();
            orderAvailablePaneChangerParameters.ordersScrollPane().toFront();

            PieChart pieChart = orderAvailablePaneChangerParameters.pieChart();
            List<String> colors = orderAvailablePaneChangerParameters.colors();
            orderAvailablePaneChangerParameters.profileControllerService().fillPieChartWithData(pieChart, colors);

            VBox stocksVBox = orderAvailablePaneChangerParameters.stocksVBox();
            orderAvailablePaneChangerParameters.profileControllerService().fillStocksVBoxWithData(colors, stocksVBox);

            VBox ordersVBox = orderAvailablePaneChangerParameters.ordersVBox();
            orderAvailablePaneChangerParameters.profileControllerService().fillOrdersVBox(ordersVBox);
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
                .noneMatch(entry -> entry.getValue() > 0);
    }
}
