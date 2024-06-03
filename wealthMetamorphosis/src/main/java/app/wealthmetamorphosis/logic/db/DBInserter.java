package app.wealthmetamorphosis.logic.db;

import app.wealthmetamorphosis.data.DBConnection;
import app.wealthmetamorphosis.data.Order;
import app.wealthmetamorphosis.data.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBInserter {
    private final DBConnection dbConnection;

    public DBInserter(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void insertIntoUsers(User newUser) {
        String query = "INSERT INTO users (user_id, user_name, user_password) VALUES (?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbConnection.DBUrl(), dbConnection.user(), dbConnection.password());
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newUser.getUserId());
            preparedStatement.setString(2, newUser.getUserName());
            preparedStatement.setString(3, newUser.getPassword());
            preparedStatement.execute();
            System.out.println("Done");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertIntoOrders(Order newOrder) {
        String query = "INSERT INTO orders (order_id, stock_symbol, stock_price, stock_shares, order_type, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DriverManager.getConnection(dbConnection.DBUrl(), dbConnection.user(), dbConnection.password());
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, newOrder.getOrderId());
            preparedStatement.setString(2, newOrder.getStockSymbol());
            preparedStatement.setDouble(3, newOrder.getStockPrice());
            preparedStatement.setDouble(4, newOrder.getStockShares());
            preparedStatement.setString(5, newOrder.getOrderType().name());
            preparedStatement.setString(6, newOrder.getUserId());
            preparedStatement.execute();
            System.out.println("Done");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
