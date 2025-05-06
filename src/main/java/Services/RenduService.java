package Services;

import Entities.Rendu;
import Utils.MyDB;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RenduService {
    private Connection cnx;

    public RenduService() {
        cnx = MyDB.getInstance().getCon();
    }

    public void addRendu(Rendu r) throws SQLException {
        String query = "INSERT INTO rendu (message_rendu, type_rendu, Date_envoi_rendu) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, r.getMessage_rendu());
            pst.setString(2, r.getType_rendu());
            pst.setDate(3, Date.valueOf(r.getDate_envoi_rendu()));
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    r.setId_rendu(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Rendu> getAllRendus() throws SQLException {
        List<Rendu> rendus = new ArrayList<>();
        String query = "SELECT * FROM rendu";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Rendu r = new Rendu(
                        rs.getString("message_rendu"),
                        rs.getString("type_rendu"),
                        rs.getDate("Date_envoi_rendu").toLocalDate()
                );
                r.setId_rendu(rs.getInt("id_rendu"));
                rendus.add(r);
            }
        }
        return rendus;
    }

    public void updateRendu(Rendu r) throws SQLException {
        String query = "UPDATE rendu SET message_rendu=?, type_rendu=?, Date_envoi_rendu=? WHERE id_rendu=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, r.getMessage_rendu());
            pst.setString(2, r.getType_rendu());
            pst.setDate(3, Date.valueOf(r.getDate_envoi_rendu()));
            pst.setInt(4, r.getId_rendu());
            pst.executeUpdate();
        }
    }

    public void deleteRendu(int id) throws SQLException {
        String query = "DELETE FROM rendu WHERE id_rendu=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public Rendu getRenduById(int id) throws SQLException {
        String query = "SELECT * FROM rendu WHERE id_rendu=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Rendu r = new Rendu(
                        rs.getString("message_rendu"),
                        rs.getString("type_rendu"),
                        rs.getDate("Date_envoi_rendu").toLocalDate()
                );
                r.setId_rendu(rs.getInt("id_rendu"));
                return r;
            }
        }
        return null;
    }
}