package Services;

import Entites.Category;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCategorie {
    private Connection connection;

    public ServiceCategorie() throws SQLException {
        this.connection = MyDB.getInstance().getCon();
    }

    public List<Category> afficher() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories";

        try {
            // Get a fresh connection for each operation
            connection = MyDB.getInstance().getCon();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Category categorie = new Category();
                categorie.setId(resultSet.getInt("id"));
                categorie.setNomCategories(resultSet.getString("nom_categories"));
                categorie.setDescriptionCategories(resultSet.getString("description_categories"));
                categories.add(categorie);
            }

            // Close resources
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            System.err.println("Error in afficher(): " + e.getMessage());
            throw e;
        }

        return categories;
    }

    public void ajouter(Category categorie) throws SQLException {
        String query = "INSERT INTO categories (nom_categories, description_categories) VALUES (?, ?)";
        try {
            connection = MyDB.getInstance().getCon();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, categorie.getNomCategories());
            ps.setString(2, categorie.getDescriptionCategories());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error in ajouter(): " + e.getMessage());
            throw e;
        }
    }

    public void modifier(Category categorie) throws SQLException {
        String query = "UPDATE categories SET nom_categories = ?, description_categories = ? WHERE id = ?";
        try {
            connection = MyDB.getInstance().getCon();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, categorie.getNomCategories());
            ps.setString(2, categorie.getDescriptionCategories());
            ps.setInt(3, categorie.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error in modifier(): " + e.getMessage());
            throw e;
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM categories WHERE id = ?";
        try {
            connection = MyDB.getInstance().getCon();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            System.err.println("Error in supprimer(): " + e.getMessage());
            throw e;
        }
    }
}
