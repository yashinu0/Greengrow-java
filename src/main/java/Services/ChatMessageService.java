package Services;

import Entities.ChatMessage;
import Utils.MyDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageService {
    
    public void addChatMessage(ChatMessage chatMsg) {
        String sql = "INSERT INTO chat_message (reclamation_id, message, response, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, chatMsg.getReclamationId());
            pst.setString(2, chatMsg.getMessage());
            pst.setString(3, chatMsg.getResponse());
            pst.setTimestamp(4, Timestamp.valueOf(chatMsg.getCreatedAt()));
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ChatMessage> getChatMessagesByReclamationId(int reclamationId) {
        List<ChatMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM chat_message WHERE reclamation_id = ? ORDER BY created_at ASC";
        try (Connection conn = MyDB.getInstance().getCon();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChatMessage msg = new ChatMessage();
                msg.setId(rs.getInt("id"));
                msg.setReclamationId(rs.getInt("reclamation_id"));
                msg.setMessage(rs.getString("message"));
                msg.setResponse(rs.getString("response"));
                msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
} 