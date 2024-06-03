package app.wealthmetamorphosis.logic.db;

import app.wealthmetamorphosis.data.DBConnection;
import java.sql.*;
import java.util.List;

public class DBReader <T> {
    private final ResultSetToList<T> resultSetToList;
    private final DBConnection dbConnection;

    public DBReader(ResultSetToList<T> resultSetToList, DBConnection dbConnection) {
        this.resultSetToList = resultSetToList;
        this.dbConnection = dbConnection;
    }

    public List<T> readFromDB(String query) {
        try (Connection connection = DriverManager.getConnection(dbConnection.DBUrl(), dbConnection.user(), dbConnection.password());
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)){

            return resultSetToList.resultSetToList(resultSet);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
