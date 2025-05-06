package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDB {
    private static final String URL = "jdbc:mysql://localhost:3306/greengrow";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static MyDB instance;

    private MyDB() {
        // Le constructeur ne crée plus de connexion
    }

    public static MyDB getInstance() {
        if (instance == null) {
            instance = new MyDB();
        }
        return instance;
    }

    /**
     * Crée et retourne une nouvelle connexion à la base de données.
     * Cette méthode crée une nouvelle connexion à chaque appel pour éviter les problèmes
     * de connexions fermées.
     * 
     * @return Une nouvelle connexion à la base de données
     */
    public Connection getCon() {
        try {
            Connection newCon = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Nouvelle connexion à la base de données établie");
            return newCon;
        } catch (SQLException e) {
            System.out.println("Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void setCon(Connection con) {
        // This method is no longer used in the new implementation
    }
}
