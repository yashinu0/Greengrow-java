package Services;

import Interfaces.ReclamationInterface;
import Entities.Reclamation;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReclamationService implements ReclamationInterface {
    private static final Logger LOGGER = Logger.getLogger(ReclamationService.class.getName());

    public ReclamationService() {
        // Le constructeur ne crée plus de connexion
    }

    public List<Reclamation> findAll() {
        return getAllReclamations();
    }

    public void delete(Reclamation reclamation) {
        deleteReclamation(reclamation.getId());
    }

    @Override
    public void addReclamation(Reclamation reclamation) {
        String query = "INSERT INTO reclamation (utilisateur_id, produit_id, description_rec, statut_rec, date_rec, message_reclamation) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, reclamation.getUtilisateur_id());
            pst.setInt(2, reclamation.getProduit_id());
            pst.setString(3, reclamation.getDescription_rec());
            pst.setString(4, reclamation.getStatut_rec());
            pst.setTimestamp(5, new Timestamp(reclamation.getDate_rec().getTime()));
            pst.setString(6, reclamation.getMessage_reclamation());
            
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating reclamation failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reclamation.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating reclamation failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding reclamation", e);
        }
    }

    @Override
    public List<Reclamation> getAllReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.nom_user, p.nom_produit FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id_user " +
                      "LEFT JOIN produit p ON r.produit_id = p.id";

        try (Connection connection = MyDB.getInstance().getCon();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Reclamation r = new Reclamation();
                r.setId(rs.getInt("id"));
                r.setUtilisateur_id(rs.getInt("utilisateur_id"));
                r.setProduit_id(rs.getInt("produit_id"));
                r.setDescription_rec(rs.getString("description_rec"));
                r.setDate_rec(rs.getTimestamp("date_rec"));
                r.setStatut_rec(rs.getString("statut_rec"));
                r.setUser_name(rs.getString("nom_user"));
                r.setProduct_name(rs.getString("nom_produit"));
                r.setMessage_reclamation(rs.getString("message_reclamation"));
                reclamations.add(r);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all reclamations", e);
        }
        return reclamations;
    }

    @Override
    public Reclamation getReclamationById(int id) {
        String query = "SELECT r.*, u.nom_user, p.nom_produit FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id_user " +
                      "LEFT JOIN produit p ON r.produit_id = p.id " +
                      "WHERE r.id = ?";

        try (Connection connection = MyDB.getInstance().getCon();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Reclamation r = new Reclamation();
                    r.setId(rs.getInt("id"));
                    r.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    r.setProduit_id(rs.getInt("produit_id"));
                    r.setDescription_rec(rs.getString("description_rec"));
                    r.setDate_rec(rs.getTimestamp("date_rec"));
                    r.setStatut_rec(rs.getString("statut_rec"));
                    r.setUser_name(rs.getString("nom_user"));
                    r.setProduct_name(rs.getString("nom_produit"));
                    r.setMessage_reclamation(rs.getString("message_reclamation"));
                    return r;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamation by ID", e);
        }
        return null;
    }

    @Override
    public void updateReclamation(Reclamation reclamation) {
        String query = "UPDATE reclamation SET description_rec = ?, statut_rec = ?, message_reclamation = ? WHERE id = ?";
        
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, reclamation.getDescription_rec());
            pst.setString(2, reclamation.getStatut_rec());
            pst.setString(3, reclamation.getMessage_reclamation());
            pst.setInt(4, reclamation.getId());
            
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reclamation", e);
        }
    }

    @Override
    public void deleteReclamation(int id) {
        // Supprimer d'abord les messages associés
        String deleteMessagesQuery = "DELETE FROM reclamation_message WHERE reclamation_id = ?";
        String deleteChatMessagesQuery = "DELETE FROM chat_message WHERE reclamation_id = ?";
        String deleteReclamationQuery = "DELETE FROM reclamation WHERE id = ?";
        
        try (Connection conn = MyDB.getInstance().getCon()) {
            // Désactiver l'auto-commit pour effectuer une transaction
            conn.setAutoCommit(false);
            
            try {
                // Supprimer les messages de réclamation
                try (PreparedStatement pst = conn.prepareStatement(deleteMessagesQuery)) {
                    pst.setInt(1, id);
                    pst.executeUpdate();
                }
                
                // Supprimer les messages de chat
                try (PreparedStatement pst = conn.prepareStatement(deleteChatMessagesQuery)) {
                    pst.setInt(1, id);
                    pst.executeUpdate();
                }
                
                // Supprimer la réclamation
                try (PreparedStatement pst = conn.prepareStatement(deleteReclamationQuery)) {
                    pst.setInt(1, id);
                    pst.executeUpdate();
                }
                
                // Valider la transaction
                conn.commit();
            } catch (SQLException e) {
                // En cas d'erreur, annuler la transaction
                conn.rollback();
                throw e;
            } finally {
                // Réactiver l'auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting reclamation", e);
        }
    }

    @Override
    public List<Reclamation> getReclamationsByUser(int utilisateur_id) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.nom_user, p.nom_produit FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id_user " +
                      "LEFT JOIN produit p ON r.produit_id = p.id " +
                      "WHERE r.utilisateur_id = ?";

        try (Connection connection = MyDB.getInstance().getCon();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            
            pstmt.setInt(1, utilisateur_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reclamation r = new Reclamation();
                    r.setId(rs.getInt("id"));
                    r.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    r.setProduit_id(rs.getInt("produit_id"));
                    r.setDescription_rec(rs.getString("description_rec"));
                    r.setDate_rec(rs.getTimestamp("date_rec"));
                    r.setStatut_rec(rs.getString("statut_rec"));
                    r.setUser_name(rs.getString("nom_user"));
                    r.setProduct_name(rs.getString("nom_produit"));
                    r.setMessage_reclamation(rs.getString("message_reclamation"));
                    reclamations.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamations by user", e);
        }
        return reclamations;
    }

    @Override
    public List<Reclamation> getReclamationsByProduct(int produit_id) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.nom_user, p.nom_produit FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id_user " +
                      "LEFT JOIN produit p ON r.produit_id = p.id " +
                      "WHERE r.produit_id = ?";

        try (Connection connection = MyDB.getInstance().getCon();
             PreparedStatement pstmt = connection.prepareStatement(query)) {
            
            pstmt.setInt(1, produit_id);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Reclamation r = new Reclamation();
                    r.setId(rs.getInt("id"));
                    r.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    r.setProduit_id(rs.getInt("produit_id"));
                    r.setDescription_rec(rs.getString("description_rec"));
                    r.setDate_rec(rs.getTimestamp("date_rec"));
                    r.setStatut_rec(rs.getString("statut_rec"));
                    r.setUser_name(rs.getString("nom_user"));
                    r.setProduct_name(rs.getString("nom_produit"));
                    r.setMessage_reclamation(rs.getString("message_reclamation"));
                    reclamations.add(r);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamations by product", e);
        }
        return reclamations;
    }

    @Override
    public void updateReclamationStatus(int id, String newStatus) {
        String query = "UPDATE reclamation SET statut_rec = ? WHERE id = ?";
        
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, newStatus);
            pst.setInt(2, id);
            
            pst.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reclamation status", e);
        }
    }
}