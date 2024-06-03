package app.wealthmetamorphosis.logic.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ResultSetToList <T>{
    List<T> resultSetToList(ResultSet resultSet) throws SQLException;
}
