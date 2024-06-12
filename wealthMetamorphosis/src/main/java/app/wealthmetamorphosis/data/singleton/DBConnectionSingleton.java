package app.wealthmetamorphosis.data.singleton;

import app.wealthmetamorphosis.data.DBConnection;

public class DBConnectionSingleton {
    private static DBConnection dbConnection;

    public DBConnectionSingleton() {
    }
    public static synchronized DBConnection getInstance() {
        return dbConnection;
    }

    public static void setDbConnection(DBConnection dbConnection) {
        DBConnectionSingleton.dbConnection = dbConnection;
    }
}
