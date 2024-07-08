package app.wealthmetamorphosis.data.parameters;

import app.wealthmetamorphosis.logic.service.ProfileControllerService;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.List;

public record OrderAvailablePaneChangerParameters(VBox progressVBox, ScrollPane ordersScrollPane, ProfileControllerService profileControllerService,
                                                  PieChart pieChart, List<String> colors, VBox stocksVBox,
                                                  VBox ordersVBox) {
}
