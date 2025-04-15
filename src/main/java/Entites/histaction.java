package Entites;

import java.time.LocalDateTime;

public class histaction {
    private int id_action;
      // Clé étrangère pour l'utilisateur
    private utilisateur user;
    private String type_action;
    private String description_action;
    private LocalDateTime date_action;  // Changement de Date à LocalDateTime

    public histaction() {}

    public histaction(utilisateur user, String type_action, String description_action, LocalDateTime date_action) {
        this.user = user;
        this.type_action = type_action;
        this.description_action = description_action;
        this.date_action = date_action;
    }

    // Getters et Setters
    public int getId_action() {
        return id_action;
    }

    public void setId_action(int id_action) {
        this.id_action = id_action;
    }

    public utilisateur getUser() {
        return user;
    }

    public void setUser(utilisateur user) {
        this.user = user;
    }

    public String getType_action() {
        return type_action;
    }

    public void setType_action(String type_action) {
        this.type_action = type_action;
    }

    public String getDescription_action() {
        return description_action;
    }

    public void setDescription_action(String description_action) {
        this.description_action = description_action;
    }

    public LocalDateTime getDate_action() {
        return date_action;
    }

    public void setDate_action(LocalDateTime date_action) {
        this.date_action = date_action;
    }
}
