package com.workscheduler.database;

import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:work_scheduler.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "full_name TEXT NOT NULL," +
                "role TEXT DEFAULT 'USER'," +
                "created_at INTEGER NOT NULL" +
                ")";

        String createTasks = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "category TEXT," +
                "start_time TEXT," +
                "end_time TEXT," +
                "estimated_minutes INTEGER DEFAULT 0," +
                "actual_minutes INTEGER DEFAULT 0," +
                "status TEXT DEFAULT 'PENDING'," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")";

        String createLogs = "CREATE TABLE IF NOT EXISTS action_logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "username TEXT NOT NULL," +
                "action TEXT NOT NULL," +
                "details TEXT," +
                "timestamp INTEGER NOT NULL," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createTasks);
            stmt.execute(createLogs);

            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                String insertAdmin = "INSERT INTO users (username, password_hash, full_name, role, created_at) VALUES ('admin', 'admin123', 'Administrator', 'ADMIN', ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(insertAdmin)) {
                    pstmt.setLong(1, System.currentTimeMillis());
                    pstmt.execute();
                }
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}