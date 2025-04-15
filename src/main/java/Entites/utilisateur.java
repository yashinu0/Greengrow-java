package Entites;

import java.util.List;
import java.util.Objects;

public class utilisateur {
    private int id_user;
    private String nom_user;
    private String prenom_user;
    private String email_user;
    private String mot_de_passe_user;
    private String role_user;
    private String adresse_user;
    private String code_postal_user;
    private String telephone_user;
    private String ville_user;
    private boolean is_active;
    private List<histaction> actions;

    public utilisateur() {}

    public utilisateur(int id_user, String nom_user, String prenom_user, String email_user, String mot_de_passe_user,
                       String role_user, String adresse_user, String code_postal_user, String telephone_user,
                       String ville_user, boolean is_active) {
        this.id_user = id_user;
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.email_user = email_user;
        this.mot_de_passe_user = mot_de_passe_user;
        this.role_user = role_user;
        this.adresse_user = adresse_user;
        this.code_postal_user = code_postal_user;
        this.telephone_user = telephone_user;
        this.ville_user = ville_user;
        this.is_active = is_active;
    }

    public utilisateur(String nom_user, String prenom_user, String email_user, String mot_de_passe_user,
                       String role_user, String adresse_user, String code_postal_user, String telephone_user,
                       String ville_user, boolean is_active) {
        this.nom_user = nom_user;
        this.prenom_user = prenom_user;
        this.email_user = email_user;
        this.mot_de_passe_user = mot_de_passe_user;
        this.role_user = role_user;
        this.adresse_user = adresse_user;
        this.code_postal_user = code_postal_user;
        this.telephone_user = telephone_user;
        this.ville_user = ville_user;
        this.is_active = is_active;
    }

    public List<histaction> getActions() {
        return actions;
    }

    public void setActions(List<histaction> actions) {
        this.actions = actions;
    }

    // Getters et Setters
    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getNom_user() {
        return nom_user;
    }

    public void setNom_user(String nom_user) {
        this.nom_user = nom_user;
    }

    public String getPrenom_user() {
        return prenom_user;
    }

    public void setPrenom_user(String prenom_user) {
        this.prenom_user = prenom_user;
    }

    public String getEmail_user() {
        return email_user;
    }

    public void setEmail_user(String email_user) {
        this.email_user = email_user;
    }

    public String getMot_de_passe_user() {
        return mot_de_passe_user;
    }

    public void setMot_de_passe_user(String mot_de_passe_user) {
        this.mot_de_passe_user = mot_de_passe_user;
    }

    public String getRole_user() {
        return role_user;
    }

    public void setRole_user(String role_user) {
        this.role_user = role_user;
    }

    public String getAdresse_user() {
        return adresse_user;
    }

    public void setAdresse_user(String adresse_user) {
        this.adresse_user = adresse_user;
    }

    public String getCode_postal_user() {
        return code_postal_user;
    }

    public void setCode_postal_user(String code_postal_user) {
        this.code_postal_user = code_postal_user;
    }

    public String getTelephone_user() {
        return telephone_user;
    }

    public void setTelephone_user(String telephone_user) {
        this.telephone_user = telephone_user;
    }

    public String getVille_user() {
        return ville_user;
    }

    public void setVille_user(String ville_user) {
        this.ville_user = ville_user;
    }

    public boolean isIs_active() {
        return is_active;
    }

    public void setIs_active(boolean is_active) {
        this.is_active = is_active;
    }

    @Override
    public String toString() {
        return "utilisateur{" +
                "id_user=" + id_user +
                ", nom_user='" + nom_user + '\'' +
                ", prenom_user='" + prenom_user + '\'' +
                ", email_user='" + email_user + '\'' +
                ", mot_de_passe_user='" + mot_de_passe_user + '\'' +
                ", role_user='" + role_user + '\'' +
                ", adresse_user='" + adresse_user + '\'' +
                ", code_postal_user='" + code_postal_user + '\'' +
                ", telephone_user='" + telephone_user + '\'' +
                ", ville_user='" + ville_user + '\'' +
                ", is_active=" + is_active +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof utilisateur other)) return false;
        return getId_user() == other.getId_user() &&
                isIs_active() == other.isIs_active() &&
                Objects.equals(getNom_user(), other.getNom_user()) &&
                Objects.equals(getPrenom_user(), other.getPrenom_user()) &&
                Objects.equals(getEmail_user(), other.getEmail_user()) &&
                Objects.equals(getMot_de_passe_user(), other.getMot_de_passe_user()) &&
                Objects.equals(getRole_user(), other.getRole_user()) &&
                Objects.equals(getAdresse_user(), other.getAdresse_user()) &&
                Objects.equals(getCode_postal_user(), other.getCode_postal_user()) &&
                Objects.equals(getTelephone_user(), other.getTelephone_user()) &&
                Objects.equals(getVille_user(), other.getVille_user());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId_user(), getNom_user(), getPrenom_user(), getEmail_user(), getMot_de_passe_user(),
                getRole_user(), getAdresse_user(), getCode_postal_user(), getTelephone_user(),
                getVille_user(), isIs_active());
    }


}
