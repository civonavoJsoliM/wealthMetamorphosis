package app.wealthmetamorphosis.data.parameters;

import app.wealthmetamorphosis.logic.service.HttpService;
import app.wealthmetamorphosis.logic.service.OwnedStockService;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public record StockPriceRefresherParameters(HttpService httpService, OwnedStockService ownedStockService,
                                            TextField sellSharesTextField,
                                            TextField buySharesTextField, Label profitLossLabel, Label totalCostLabel,
                                            Label totalLabel) {
}
