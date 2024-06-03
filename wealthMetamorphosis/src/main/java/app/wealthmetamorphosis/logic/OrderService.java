package app.wealthmetamorphosis.logic;

import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.OrderType;
import app.wealthmetamorphosis.logic.db.ResultSetToList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements ResultSetToList<Order> {
    @Override
    public List<Order> resultSetToList(ResultSet resultSet) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (resultSet.next()) {
            String orderId = resultSet.getString("order_id");
            String stockSymbol = resultSet.getString("stock_symbol");
            double stockPrice = resultSet.getDouble("stock_price");
            double stockShares = resultSet.getDouble("stock_shares");
            OrderType orderType = OrderType.valueOf(resultSet.getString("order_type"));
            LocalDateTime orderTimeStamp = resultSet.getTimestamp("order_timeStamp").toLocalDateTime();
            String userId = resultSet.getString("user_id");
            Order order = new Order(orderId, stockSymbol, stockPrice, stockShares, orderType, orderTimeStamp, userId);
            orders.add(order);
        }
        return orders;
    }
}
