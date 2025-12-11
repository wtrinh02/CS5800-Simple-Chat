package db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;
    private Connection connection;

    private Database() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:chat.db");
            System.out.println("DB absolute path = " + new File("chat.db").getAbsolutePath());

        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect SQLite", e);
        }
    }

    public static synchronized Database getInstance() {
        if (instance == null)
            instance = new Database();
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
