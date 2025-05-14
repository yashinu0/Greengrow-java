package Controlles;

import Entities.Livreur;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LivreurController {
    private Connection connection;

    public LivreurController() {
        this.connection = MyDB.getInstance().getCon();
    }

    public void ajouterLivreur(Livreur livreur) throws SQLException {
        String sql = "INSERT INTO livreur (nom_livreur, prenom_livreur, numero_livreur, addresse_livreur, photo_livreur) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, livreur.getNomLivreur());
            stmt.setString(2, livreur.getPrenomLivreur());
            stmt.setString(3, livreur.getNumeroLivreur());
            stmt.setString(4, livreur.getAddresseLivreur());
            stmt.setString(5, livreur.getPhotoLivreur());
            stmt.executeUpdate();
        }
    }

    public List<Livreur> getAllLivreurs() throws SQLException {
        List<Livreur> livreurs = new ArrayList<>();
        String sql = "SELECT * FROM livreur";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Livreur livreur = new Livreur();
                livreur.setId(rs.getInt("id"));
                livreur.setNomLivreur(rs.getString("nom_livreur"));
                livreur.setPrenomLivreur(rs.getString("prenom_livreur"));
                livreur.setNumeroLivreur(rs.getString("numero_livreur"));
                livreur.setAddresseLivreur(rs.getString("addresse_livreur"));
                livreur.setPhotoLivreur(rs.getString("photo_livreur"));
                livreurs.add(livreur);
            }
        }
        return livreurs;
    }

    public Livreur getLivreurById(int id) throws SQLException {
        String sql = "SELECT * FROM livreur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Livreur livreur = new Livreur();
                    livreur.setId(rs.getInt("id"));
                    livreur.setNomLivreur(rs.getString("nom_livreur"));
                    livreur.setPrenomLivreur(rs.getString("prenom_livreur"));
                    livreur.setNumeroLivreur(rs.getString("numero_livreur"));
                    livreur.setAddresseLivreur(rs.getString("addresse_livreur"));
                    livreur.setPhotoLivreur(rs.getString("photo_livreur"));
                    return livreur;
                }
            }
        }
        return null;
    }

    public void updateLivreur(Livreur livreur) throws SQLException {
        String sql = "UPDATE livreur SET nom_livreur = ?, prenom_livreur = ?, numero_livreur = ?, addresse_livreur = ?, photo_livreur = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, livreur.getNomLivreur());
            stmt.setString(2, livreur.getPrenomLivreur());
            stmt.setString(3, livreur.getNumeroLivreur());
            stmt.setString(4, livreur.getAddresseLivreur());
            stmt.setString(5, livreur.getPhotoLivreur());
            stmt.setInt(6, livreur.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteLivreur(int id) throws SQLException {
        String sql = "DELETE FROM livreur WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
