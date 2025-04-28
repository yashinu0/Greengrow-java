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
    private final Connection connection;

    public ReclamationService() {
        this.connection = MyDB.getInstance().getCon();
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
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
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
            LOGGER.info("Reclamation added successfully with ID: " + reclamation.getId());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding reclamation", e);
            throw new RuntimeException("Error adding reclamation: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reclamation> getAllReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.* FROM reclamation r";
        
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Reclamation reclamation = new Reclamation();
                reclamation.setId(rs.getInt("id"));
                reclamation.setUtilisateur_id(rs.getInt("utilisateur_id"));
                reclamation.setProduit_id(rs.getInt("produit_id"));
                reclamation.setDescription_rec(rs.getString("description_rec"));
                reclamation.setStatut_rec(rs.getString("statut_rec"));
                reclamation.setDate_rec(rs.getTimestamp("date_rec"));
                reclamation.setMessage_reclamation(rs.getString("message_reclamation"));
                // Set default values for user_name and product_name
                reclamation.setUser_name("User " + rs.getInt("utilisateur_id"));
                reclamation.setProduct_name("Product " + rs.getInt("produit_id"));
                reclamations.add(reclamation);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamations", e);
            throw new RuntimeException("Error getting reclamations: " + e.getMessage(), e);
        }
        return reclamations;
    }

    @Override
    public Reclamation getReclamationById(int id) {
        String query = "SELECT r.*, u.nom as user_name, p.nom as product_name " +
                      "FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id " +
                      "LEFT JOIN produit p ON r.produit_id = p.id " +
                      "WHERE r.id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReclamation(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamation by ID: " + id, e);
            throw new RuntimeException("Error getting reclamation: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void updateReclamation(Reclamation reclamation) {
        try {
            String query = "UPDATE reclamation SET statut_rec = ? WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, reclamation.getStatut_rec());
            statement.setInt(2, reclamation.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reclamation", e);
            throw new RuntimeException("Error updating reclamation: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteReclamation(int id) {
        try {
            connection.setAutoCommit(false);
            
            // Delete associated messages
            String deleteMessagesQuery = "DELETE FROM reclamation_message WHERE reclamation_id = ?";
            try (PreparedStatement deleteMessagesStmt = connection.prepareStatement(deleteMessagesQuery)) {
                deleteMessagesStmt.setInt(1, id);
                deleteMessagesStmt.executeUpdate();
            }

            // Delete the reclamation
            String deleteReclamationQuery = "DELETE FROM reclamation WHERE id = ?";
            try (PreparedStatement deleteReclamationStmt = connection.prepareStatement(deleteReclamationQuery)) {
                deleteReclamationStmt.setInt(1, id);
                int affectedRows = deleteReclamationStmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Deleting reclamation failed, no rows affected.");
                }
            }

            connection.commit();
            LOGGER.info("Reclamation deleted successfully with ID: " + id);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
            }
            LOGGER.log(Level.SEVERE, "Error deleting reclamation", e);
            throw new RuntimeException("Error deleting reclamation: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error resetting auto-commit", e);
            }
        }
    }

    @Override
    public List<Reclamation> getReclamationsByUser(int utilisateur_id) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.* FROM reclamation r WHERE r.utilisateur_id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, utilisateur_id);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Reclamation reclamation = new Reclamation();
                    reclamation.setId(rs.getInt("id"));
                    reclamation.setUtilisateur_id(rs.getInt("utilisateur_id"));
                    reclamation.setProduit_id(rs.getInt("produit_id"));
                    reclamation.setDescription_rec(rs.getString("description_rec"));
                    reclamation.setStatut_rec(rs.getString("statut_rec"));
                    reclamation.setDate_rec(rs.getTimestamp("date_rec"));
                    reclamation.setMessage_reclamation(rs.getString("message_reclamation"));
                    reclamations.add(reclamation);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamations by user: " + utilisateur_id, e);
            throw new RuntimeException("Error getting user reclamations: " + e.getMessage(), e);
        }
        return reclamations;
    }

    @Override
    public List<Reclamation> getReclamationsByProduct(int produit_id) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, u.nom as user_name, p.nom as product_name " +
                      "FROM reclamation r " +
                      "LEFT JOIN utilisateur u ON r.utilisateur_id = u.id " +
                      "LEFT JOIN produit p ON r.produit_id = p.id " +
                      "WHERE r.produit_id = ?";
        
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, produit_id);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    reclamations.add(mapResultSetToReclamation(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting reclamations by product: " + produit_id, e);
            throw new RuntimeException("Error getting product reclamations: " + e.getMessage(), e);
        }
        return reclamations;
    }

    @Override
    public void updateReclamationStatus(int id, String newStatus) {
        String query = "UPDATE reclamation SET statut_rec = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, newStatus);
            pst.setInt(2, id);
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating reclamation status failed, no rows affected.");
            }
            LOGGER.info("Reclamation status updated successfully for ID: " + id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating reclamation status", e);
            throw new RuntimeException("Error updating reclamation status: " + e.getMessage(), e);
        }
    }

    private Reclamation mapResultSetToReclamation(ResultSet rs) throws SQLException {
        Reclamation reclamation = new Reclamation();
        reclamation.setId(rs.getInt("id"));
        reclamation.setUtilisateur_id(rs.getInt("utilisateur_id"));
        reclamation.setProduit_id(rs.getInt("produit_id"));
        reclamation.setDescription_rec(rs.getString("description_rec"));
        reclamation.setStatut_rec(rs.getString("statut_rec"));
        reclamation.setDate_rec(rs.getTimestamp("date_rec"));
        reclamation.setMessage_reclamation(rs.getString("message_reclamation"));
        reclamation.setUser_name(rs.getString("user_name"));
        reclamation.setProduct_name(rs.getString("product_name"));
        return reclamation;
    }
}