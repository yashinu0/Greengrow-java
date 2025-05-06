package Services;

import Entites.Produit;

import Entites.Category;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceProduit {

    public void ajouter(Produit produit) throws SQLException {
        String query = "INSERT INTO produit (id_categories_id, quantite, nom_produit, description_produit, " +
                "prix_produit, disponibilte_produit, image_produit, location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, produit.getIdCategories());
            stmt.setInt(2, produit.getQuantite());
            stmt.setString(3, produit.getNomProduit());
            stmt.setString(4, produit.getDescriptionProduit());
            stmt.setInt(5, produit.getPrixProduit());
            stmt.setString(6, produit.getDisponibilteProduit());
            stmt.setString(7, produit.getImageProduit());
            stmt.setString(8, produit.getLocation());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating produit failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    produit.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating produit failed, no ID obtained.");
                }
            }
        }
    }

    public void modifier(Produit produit) throws SQLException {
        String query = "UPDATE produit SET id_categories_id = ?, quantite = ?, nom_produit = ?, " +
                "description_produit = ?, prix_produit = ?, disponibilte_produit = ?, " +
                "image_produit = ?, location = ? WHERE id = ?";

        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, produit.getIdCategories());
            stmt.setInt(2, produit.getQuantite());
            stmt.setString(3, produit.getNomProduit());
            stmt.setString(4, produit.getDescriptionProduit());
            stmt.setInt(5, produit.getPrixProduit());
            stmt.setString(6, produit.getDisponibilteProduit());
            stmt.setString(7, produit.getImageProduit());
            stmt.setString(8, produit.getLocation());
            stmt.setInt(9, produit.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating produit failed, no rows affected.");
            }
        }
    }

    public void supprimer(int id) throws SQLException {
        String query = "DELETE FROM produit WHERE id = ?";

        try (Connection conn =MyDB.getInstance().getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Deleting produit failed, no rows affected.");
            }
        }
    }

    public List<Produit> afficher() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String query = "SELECT * FROM produit";

        try (Connection conn = MyDB.getInstance().getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Produit p = new Produit();
                p.setId(rs.getInt("id"));
                p.setIdCategories(rs.getInt("id_categories_id"));
                p.setQuantite(rs.getInt("quantite"));
                p.setNomProduit(rs.getString("nom_produit"));
                p.setDescriptionProduit(rs.getString("description_produit"));
                p.setPrixProduit(rs.getInt("prix_produit"));
                p.setDisponibilteProduit(rs.getString("disponibilte_produit"));
                p.setImageProduit(rs.getString("image_produit"));
                p.setLocation(rs.getString("location"));

                produits.add(p);
            }
        }
        return produits;
    }

    public boolean categoryExists(int categoryId) throws SQLException {
        String query = "SELECT COUNT(*) FROM categories WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM categories";

        try (Connection conn = MyDB.getInstance().getCon();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Category c = new Category();
                c.setId(rs.getInt("id"));
                c.setNomCategories(rs.getString("nom_categories"));
                c.setDescriptionCategories(rs.getString("description_categories"));
                categories.add(c);
            }
        }
        return categories;
    }

    public String getCategoryName(int categoryId) throws SQLException {
        String query = "SELECT nom_categories FROM categories WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nom_categories");
            }
        }
        return "Inconnue";
    }

    public Produit findById(int id) throws SQLException {
        String query = "SELECT * FROM produit WHERE id = ?";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Produit p = new Produit();
                    p.setId(rs.getInt("id"));
                    p.setIdCategories(rs.getInt("id_categories_id"));
                    p.setQuantite(rs.getInt("quantite"));
                    p.setNomProduit(rs.getString("nom_produit"));
                    p.setDescriptionProduit(rs.getString("description_produit"));
                    p.setPrixProduit(rs.getInt("prix_produit"));
                    p.setDisponibilteProduit(rs.getString("disponibilte_produit"));
                    p.setImageProduit(rs.getString("image_produit"));
                    p.setLocation(rs.getString("location"));
                    return p;
                }
            }
        }
        return null;
    }
}