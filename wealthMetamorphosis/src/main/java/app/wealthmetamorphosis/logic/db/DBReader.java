package app.wealthmetamorphosis.logic.db;

import app.wealthmetamorphosis.data.singleton.DBConnectionSingleton;
import java.sql.*;
import java.util.List;

public class DBReader <T> {
    private final ResultSetToList<T> resultSetToList;

    public DBReader(ResultSetToList<T> resultSetToList) {
        this.resultSetToList = resultSetToList;
    }

    public List<T> readFromDB(String query) {
        try (Connection connection = DriverManager.getConnection(DBConnectionSingleton.getInstance().DBUrl(),
                DBConnectionSingleton.getInstance().user(), DBConnectionSingleton.getInstance().password());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)){

            return resultSetToList.resultSetToList(resultSet);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
