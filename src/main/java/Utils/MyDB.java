package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private static MyDB instance;
    private Connection con;
    private final String URL = "jdbc:mysql://localhost:3306/greengrow";
    private final String USER = "root";
    private final String PASSWORD = "";

    private MyDB() {
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion établie");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    public Connection getCon() {
        return con;
    }
}
