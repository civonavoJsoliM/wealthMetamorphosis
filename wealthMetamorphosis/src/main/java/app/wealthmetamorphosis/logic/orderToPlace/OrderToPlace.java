package app.wealthmetamorphosis.logic.orderToPlace;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;

public interface OrderToPlace {
    void place(OrderType orderType);
}
