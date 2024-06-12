package app.wealthmetamorphosis.logic.service;

import app.wealthmetamorphosis.data.User;
import app.wealthmetamorphosis.logic.db.ResultSetToList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ResultSetToList<User> {
    @Override
    public List<User> resultSetToList(ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            String userId = resultSet.getString("user_id");
            String userName = resultSet.getString("user_name");
            String password = resultSet.getString("user_password");
            LocalDateTime registered = resultSet.getTimestamp("registered").toLocalDateTime();
            users.add(new User(userId, userName, password, registered));
        }
        return users;
    }
}
