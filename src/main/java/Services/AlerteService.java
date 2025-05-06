package Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import Entities.Alerte;
import Utils.MyDB;

public class AlerteService {
    private Connection cnx;

    public AlerteService() {
        cnx = MyDB.getInstance().getCon();
    }

    public void addAlerte(Alerte a) throws SQLException {
        String query = "INSERT INTO alerte (Niveau_urgence_alerte, temps_limite_alerte, id_rendu) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, a.getNiveau_urgence_alerte());
            pst.setTimestamp(2, Timestamp.valueOf(a.getTemps_limite_alerte()));
            pst.setInt(3, a.getRendu() != null ? a.getRendu().getId_rendu() : 0);
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    a.setId_alerte(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Alerte> getAllAlertes() throws SQLException {
        List<Alerte> alertes = new ArrayList<>();
        String query = "SELECT * FROM alerte";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Alerte a = new Alerte(
                        rs.getString("Niveau_urgence_alerte"),
                        rs.getTimestamp("temps_limite_alerte").toLocalDateTime()
                );
                a.setId_alerte(rs.getInt("id_alerte"));
                // Fetch and set the plant (Rendu) object if id_rendu exists
                try {
                    int idRendu = rs.getInt("id_rendu");
                    if (!rs.wasNull()) {
                        RenduService renduService = new RenduService();
                        Entities.Rendu rendu = renduService.getRenduById(idRendu);
                        a.setRendu(rendu);
                    }
                } catch (Exception ignored) {}
                alertes.add(a);
            }
        }
        return alertes;
    }

    public void updateAlerte(Alerte a) throws SQLException {
        String query = "UPDATE alerte SET Niveau_urgence_alerte=?, temps_limite_alerte=? WHERE id_alerte=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, a.getNiveau_urgence_alerte());
            pst.setTimestamp(2, Timestamp.valueOf(a.getTemps_limite_alerte()));
            pst.setInt(3, a.getId_alerte());
            pst.executeUpdate();
        }
    }

    public void deleteAlerte(int id) throws SQLException {
        String query = "DELETE FROM alerte WHERE id_alerte=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public Alerte getAlerteById(int id) throws SQLException {
        String query = "SELECT * FROM alerte WHERE id_alerte=?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Alerte a = new Alerte(
                        rs.getString("Niveau_urgence_alerte"),
                        rs.getTimestamp("temps_limite_alerte").toLocalDateTime()
                );
                a.setId_alerte(rs.getInt("id_alerte"));
                return a;
            }
        }
        return null;
    }
}