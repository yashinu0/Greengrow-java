package Entities;

import java.sql.Timestamp;

public class ReclamationMessage {
    private int id;
    private int reclamation_id;
    private int sender_id;
    private String content;
    private Timestamp sent_at;
    private boolean is_from_admin;

    // Constructeurs
    public ReclamationMessage() {
    }

    public ReclamationMessage(int reclamation_id, int sender_id, String content, boolean is_from_admin) {
        this.reclamation_id = reclamation_id;
        this.sender_id = sender_id;
        this.content = content;
        this.is_from_admin = is_from_admin;
        this.sent_at = new Timestamp(System.currentTimeMillis());
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReclamation_id() {
        return reclamation_id;
    }

    public void setReclamation_id(int reclamation_id) {
        this.reclamation_id = reclamation_id;
    }

    public int getSender_id() {
        return sender_id;
    }

    public void setSender_id(int sender_id) {
        this.sender_id = sender_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getSent_at() {
        return sent_at;
    }

    public void setSent_at(Timestamp sent_at) {
        this.sent_at = sent_at;
    }

    public boolean isIs_from_admin() {
        return is_from_admin;
    }

    public void setIs_from_admin(boolean is_from_admin) {
        this.is_from_admin = is_from_admin;
    }

    @Override
    public String toString() {
        return "ReclamationMessage{" +
                "id=" + id +
                ", reclamation_id=" + reclamation_id +
                ", sender_id=" + sender_id +
                ", content='" + content + '\'' +
                ", sent_at=" + sent_at +
                ", is_from_admin=" + is_from_admin +
                '}';
    }
} 