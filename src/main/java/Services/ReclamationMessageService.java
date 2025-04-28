package Services;

import Entities.ReclamationMessage;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReclamationMessageService {
    private Connection conn;
    private PreparedStatement pst;

    public ReclamationMessageService() {
        conn = MyDB.getInstance().getCon();
    }

    public void addMessage(ReclamationMessage message) {
        String query = "INSERT INTO reclamation_message (reclamation_id, sender_id, content, sent_at, is_from_admin) VALUES (?, ?, ?, ?, ?)";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, message.getReclamation_id());
            pst.setInt(2, message.getSender_id());
            pst.setString(3, message.getContent());
            pst.setTimestamp(4, message.getSent_at());
            pst.setBoolean(5, message.isIs_from_admin());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public List<ReclamationMessage> getMessagesByReclamationId(int reclamationId) {
        List<ReclamationMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM reclamation_message WHERE reclamation_id = ? ORDER BY sent_at ASC";
        
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                ReclamationMessage message = new ReclamationMessage();
                message.setId(rs.getInt("id"));
                message.setReclamation_id(rs.getInt("reclamation_id"));
                message.setSender_id(rs.getInt("sender_id"));
                message.setContent(rs.getString("content"));
                message.setSent_at(rs.getTimestamp("sent_at"));
                message.setIs_from_admin(rs.getBoolean("is_from_admin"));
                messages.add(message);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        
        return messages;
    }

    public void deleteMessage(int messageId) {
        String query = "DELETE FROM reclamation_message WHERE id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setInt(1, messageId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateMessage(ReclamationMessage message) {
        String query = "UPDATE reclamation_message SET content = ? WHERE id = ?";
        try {
            pst = conn.prepareStatement(query);
            pst.setString(1, message.getContent());
            pst.setInt(2, message.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
} 