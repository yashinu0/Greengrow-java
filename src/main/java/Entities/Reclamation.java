package Entities;

import java.util.Date;

public class Reclamation {
    private int id;
    private int utilisateur_id;
    private int produit_id;
    private String description_rec;
    private String statut_rec;
    private Date date_rec;
    private String message_reclamation;
    private String user_name;
    private String product_name;
    private String type_rec;

    // Constructors
    public Reclamation() {
        this.date_rec = new Date();
        this.statut_rec = "Pending";
    }

    public Reclamation(int utilisateur_id, int produit_id, String description_rec, String message_reclamation) {
        this();
        this.utilisateur_id = utilisateur_id;
        this.produit_id = produit_id;
        this.description_rec = description_rec;
        this.message_reclamation = message_reclamation;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUtilisateur_id() {
        return utilisateur_id;
    }

    public void setUtilisateur_id(int utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
    }

    public int getProduit_id() {
        return produit_id;
    }

    public void setProduit_id(int produit_id) {
        this.produit_id = produit_id;
    }

    public String getDescription_rec() {
        return description_rec;
    }

    public void setDescription_rec(String description_rec) {
        this.description_rec = description_rec;
    }

    public String getStatut_rec() {
        return statut_rec;
    }

    public void setStatut_rec(String statut_rec) {
        this.statut_rec = statut_rec;
    }

    public Date getDate_rec() {
        return date_rec;
    }

    public void setDate_rec(Date date_rec) {
        this.date_rec = date_rec;
    }

    public String getMessage_reclamation() {
        return message_reclamation;
    }

    public void setMessage_reclamation(String message_reclamation) {
        this.message_reclamation = message_reclamation;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getType_rec() {
        return type_rec;
    }

    public void setType_rec(String type_rec) {
        this.type_rec = type_rec;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id=" + id +
                ", utilisateur_id=" + utilisateur_id +
                ", produit_id=" + produit_id +
                ", description_rec='" + description_rec + '\'' +
                ", statut_rec='" + statut_rec + '\'' +
                ", date_rec=" + date_rec +
                ", message_reclamation='" + message_reclamation + '\'' +
                ", user_name='" + user_name + '\'' +
                ", product_name='" + product_name + '\'' +
                ", type_rec='" + type_rec + '\'' +
                '}';
    }
}
