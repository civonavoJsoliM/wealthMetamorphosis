package app.wealthmetamorphosis.logic.paneChanger;

import app.wealthmetamorphosis.data.singleton.UserSingleton;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class NoOrdersPaneChanger implements PaneChanger {
    private final VBox noStocksToDisplayVBox;
    private final VBox noOrdersToDisplayVBox;
    private final Label portfolioWorthLabel;

    public NoOrdersPaneChanger(VBox noStocksToDisplayVBox, VBox noOrdersToDisplayVBox, Label portfolioWorthLabel) {
        this.noStocksToDisplayVBox = noStocksToDisplayVBox;
        this.noOrdersToDisplayVBox = noOrdersToDisplayVBox;
        this.portfolioWorthLabel = portfolioWorthLabel;
    }

    @Override
    public void change() {
        if (UserSingleton.getInstance().getOrders().isEmpty()) {
            noStocksToDisplayVBox.toFront();
            noOrdersToDisplayVBox.toFront();
            portfolioWorthLabel.setText("0$");
        }
    }
}
