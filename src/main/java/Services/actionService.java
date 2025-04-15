package Services;

import Entites.histaction;  // Tu utilises maintenant histaction au lieu de action
import Entites.utilisateur;
import Interfaces.InterfaceActionCRUD;
import Utils.MyDB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class actionService implements InterfaceActionCRUD<histaction> {
    Connection con;

    public actionService() {
        con = MyDB.getInstance().getCon();  // Connexion à la base de données
    }

    @Override
    public void addAction(histaction t) {  // On utilise histaction ici
        String req = "INSERT INTO `histaction` (`id_user`, `type_action`, `description_action`,`date_action`) " +
                "VALUES (?, ?, ?, ?)";  // Utilisation de PreparedStatement pour éviter les injections SQL

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, t.getUser().getId_user());  // Assumption : 'user' a un getId_utilisateur()
            pst.setString(2, t.getType_action());

            // Utilisation de la date du système (date et heure actuelles)
            LocalDateTime currentDateTime = LocalDateTime.now();
            pst.setString(3, t.getDescription_action());  // Assumption : 'user' a un getId_utilisateur()
            pst.setTimestamp(4, Timestamp.valueOf(currentDateTime));  // Insère la date du système

            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur d'insertion : " + e.getMessage());
        }
    }

    @Override
    public void updateAction(histaction t) {
        String req = "UPDATE `histaction` SET `type_action` = ?, `description_action` = ?, `date_action` = ?, `id_user` = ? WHERE `id_action` = ?";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, t.getType_action());
            pst.setString(2, t.getDescription_action());
            pst.setTimestamp(3, Timestamp.valueOf(t.getDate_action()));
            pst.setInt(4, t.getUser().getId_user());
            pst.setInt(5, t.getId_action());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur de mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void deleteAction(histaction t) {
        String req = "DELETE FROM `histaction` WHERE `id_action` = ?";

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, t.getId_action());
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur de suppression : " + e.getMessage());
        }
    }

    @Override
    public List<histaction> findAction() {
        String req = "SELECT * FROM `histaction`";
        List<histaction> actions = new ArrayList<>();

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                histaction h = new histaction();
                h.setId_action(rs.getInt("id_action"));
                h.setType_action(rs.getString("type_action"));
                h.setDescription_action(rs.getString("description_action"));
                h.setDate_action(rs.getTimestamp("date_action").toLocalDateTime());
                utilisateur user = new utilisateur();
                user.setId_user(rs.getInt("id_user"));
                h.setUser(user);
                actions.add(h);
            }
        } catch (SQLException e) {
            System.out.println("Erreur de récupération des actions : " + e.getMessage());
        }

        return actions;
    }
    public List<histaction> findActionByUserId(int id_user) {
        String req = "SELECT * FROM `histaction` WHERE `id_user` = ?";
        List<histaction> actions = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setInt(1, id_user);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                histaction h = new histaction();
                h.setId_action(rs.getInt("id_action"));
                h.setType_action(rs.getString("type_action"));
                h.setDescription_action(rs.getString("description_action"));
                h.setDate_action(rs.getTimestamp("date_action").toLocalDateTime());

                utilisateur user = new utilisateur();
                user.setId_user(rs.getInt("id_user"));
                h.setUser(user);

                actions.add(h);
            }
        } catch (SQLException e) {
            System.out.println("Erreur de récupération des actions pour l'utilisateur " + id_user + " : " + e.getMessage());
        }

        return actions;
    }



}
