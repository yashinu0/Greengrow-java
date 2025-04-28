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
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    public Connection getCon() {
        try {
            if (con == null || con.isClosed()) {
                con = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Reconnected to database");
            }
        } catch (SQLException e) {
            System.out.println("Error reconnecting to database: " + e.getMessage());
        }
        return con;
    }

    public void setCon(Connection con) {
        this.con = con;
    }
}
