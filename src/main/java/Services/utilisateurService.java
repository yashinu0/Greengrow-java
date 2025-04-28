package Services;
import Entites.utilisateur;
import Interfaces.InterfaceCRUD;
import Utils.MyDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class utilisateurService implements InterfaceCRUD<utilisateur> {
    Connection con;

    public utilisateurService() {
        con = MyDB.getInstance().getCon();
    }

    @Override
    public void add(utilisateur t) {

        String req = "INSERT INTO `utilisateur`( `nom_user`, `prenom_user`, `email_user`, `mot_de_passe_user`, `role_user`, " +
                "`adresse_user`, `code_postal_user`, `telephone_user`, `ville_user`, `is_active`) " +
                "VALUES ('" + t.getNom_user() + "', '" + t.getPrenom_user() + "', '" + t.getEmail_user() + "', '" +
                t.getMot_de_passe_user() + "', '" + t.getRole_user() + "', '" + t.getAdresse_user() + "', '" +
                t.getCode_postal_user() + "', '" + t.getTelephone_user() + "', '" + t.getVille_user() + "', " +
                t.isIs_active() + ")";


        Statement st;
        try {
            st = con.createStatement();
            st.executeUpdate(req);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void update(utilisateur t) {
        String req = "UPDATE utilisateur SET " +
                "nom_user = '" + t.getNom_user() + "', " +
                "prenom_user = '" + t.getPrenom_user() + "', " +
                "email_user = '" + t.getEmail_user() + "', " +
                "mot_de_passe_user = '" + t.getMot_de_passe_user() + "', " +
                "role_user = '" + t.getRole_user() + "', " +
                "adresse_user = '" + t.getAdresse_user() + "', " +
                "code_postal_user = '" + t.getCode_postal_user() + "', " +
                "telephone_user = '" + t.getTelephone_user() + "', " +
                "ville_user = '" + t.getVille_user() + "', " +
                "is_active = " + t.isIs_active() +
                " WHERE id_user = " + t.getId_user();

        try {
            Statement st = con.createStatement();
            st.executeUpdate(req);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public void delete(utilisateur t) {
        String req = "DELETE FROM utilisateur WHERE id_user = " + t.getId_user();
        try {
            Statement st = con.createStatement();
            st.executeUpdate(req);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<utilisateur> find() {


        String req = "SELECT * FROM `utilisateur`";

        Statement st;
        List<utilisateur> utilisateurs = new ArrayList<>();
        try {
            st = con.createStatement();

            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                utilisateur u = new utilisateur();
                u.setId_user(rs.getInt("id_user"));
                u.setNom_user(rs.getString("nom_user"));
                u.setPrenom_user(rs.getString("prenom_user"));
                u.setEmail_user(rs.getString("email_user"));
                u.setMot_de_passe_user(rs.getString("mot_de_passe_user"));
                u.setRole_user(rs.getString("role_user"));
                u.setAdresse_user(rs.getString("adresse_user"));
                u.setCode_postal_user(rs.getString("code_postal_user"));
                u.setTelephone_user(rs.getString("telephone_user"));
                u.setVille_user(rs.getString("ville_user"));
                u.setIs_active(rs.getBoolean("is_active"));

                utilisateurs.add(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return utilisateurs;

    }
    public int addAndReturnId(utilisateur t) {
        String sql = "INSERT INTO `utilisateur`(nom_user, prenom_user, email_user, mot_de_passe_user, role_user, adresse_user, code_postal_user, telephone_user, ville_user, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, t.getNom_user());
            pst.setString(2, t.getPrenom_user());
            pst.setString(3, t.getEmail_user());
            pst.setString(4, t.getMot_de_passe_user());
            pst.setString(5, t.getRole_user());
            pst.setString(6, t.getAdresse_user());
            pst.setString(7, t.getCode_postal_user());
            pst.setString(8, t.getTelephone_user());
            pst.setString(9, t.getVille_user());
            pst.setBoolean(10, t.isIs_active());

            pst.executeUpdate();

            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.out.println("Erreur d'ajout avec ID : " + e.getMessage());
        }
        return -1;
    }

    public void desactiver(int  id) {
        String req = "UPDATE utilisateur SET " +
                "is_active = 0"+
                " WHERE id_user = " + id;

        try {
            Statement st = con.createStatement();
            st.executeUpdate(req);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public utilisateur login(String email, String password) {
        String sql = "SELECT * FROM utilisateur WHERE email_user = ? AND mot_de_passe_user = ? AND is_active = 1";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, email);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new utilisateur(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("mot_de_passe_user"),
                        rs.getString("role_user"),
                        rs.getString("adresse_user"),
                        rs.getString("code_postal_user"),
                        rs.getString("telephone_user"),
                        rs.getString("ville_user"),
                        rs.getBoolean("is_active")
                );
            }

        } catch (SQLException e) {
            System.out.println("Erreur login: " + e.getMessage());
        }
        return null;
    }
    public utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email_user = ?";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new utilisateur(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("mot_de_passe_user"),
                        rs.getString("role_user"),
                        rs.getString("adresse_user"),
                        rs.getString("code_postal_user"),
                        rs.getString("telephone_user"),
                        rs.getString("ville_user"),
                        rs.getBoolean("is_active")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public boolean updatePassword(String email, String newPassword) {
        String req = "UPDATE utilisateur SET mot_de_passe_user = ? WHERE email_user = ?";
        try (PreparedStatement pst = con.prepareStatement(req)) {
            pst.setString(1, newPassword);
            pst.setString(2, email);
            int rowsAffected = pst.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("Erreur updatePassword: " + e.getMessage());
            return false;
        }
    }

    public utilisateur findByID(int id_user) {
        String sql = "SELECT * FROM utilisateur WHERE id_user = ?";
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setInt(1, id_user);

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new utilisateur(
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("prenom_user"),
                        rs.getString("email_user"),
                        rs.getString("mot_de_passe_user"),
                        rs.getString("role_user"),
                        rs.getString("adresse_user"),
                        rs.getString("code_postal_user"),
                        rs.getString("telephone_user"),
                        rs.getString("ville_user"),
                        rs.getBoolean("is_active")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}



