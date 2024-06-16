package app.wealthmetamorphosis.logic.db;

import app.wealthmetamorphosis.data.DBConnection;
import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import app.wealthmetamorphosis.data.singleton.UserSingleton;

import java.sql.*;

public class DBUpdater {

    private final DBConnection dbConnection;

    public DBUpdater(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public void updateBalance() {
        String query = "UPDATE users SET balance = ? WHERE user_id = ?";
        try (Connection connection = DriverManager.getConnection(DBConnectionSingleton.getInstance().DBUrl(), DBConnectionSingleton.getInstance().user(),
                DBConnectionSingleton.getInstance().password());
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setDouble(1, UserSingleton.getInstance().getBalance());
            preparedStatement.setString(2, UserSingleton.getInstance().getUserId());
            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
