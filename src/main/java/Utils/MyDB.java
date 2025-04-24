package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private static final String URL = "jdbc:mysql://localhost:3306/greengrow";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection con;
    private static MyDB instance;

    private MyDB() {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Create a new connection
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) {
            synchronized (MyDB.class) {
                if (instance == null) {
                    instance = new MyDB();
                }
            }
        }
        return instance;
    }

    public Connection getCon() {
        try {
            // Check if connection is closed or null
            if (con == null || con.isClosed()) {
                System.out.println("Reconnecting to database...");
                con = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            try {
                // Try to reconnect
                con = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException ex) {
                System.err.println("Failed to reconnect: " + ex.getMessage());
            }
        }
        return con;
    }

    public void closeConnection() {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
