package Entities;

import java.util.Date;

public class Feed {
    private int id;
    private String email_feed;
    private String commentaire_feed;
    private String subject_feed;
    private Date date_feed;
    private String name_feed;
    private boolean is_processed;
    private String sentiment = "NEUTRAL";

    // Constructeurs
    public Feed() {
        this.date_feed = new Date();
        this.is_processed = false;
    }

    public Feed(String email_feed, String commentaire_feed, String subject_feed, String name_feed) {
        this();
        this.email_feed = email_feed;
        this.commentaire_feed = commentaire_feed;
        this.subject_feed = subject_feed;
        this.name_feed = name_feed;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail_feed() {
        return email_feed;
    }

    public void setEmail_feed(String email_feed) {
        this.email_feed = email_feed;
    }

    public String getCommentaire_feed() {
        return commentaire_feed;
    }

    public void setCommentaire_feed(String commentaire_feed) {
        this.commentaire_feed = commentaire_feed;
    }

    public String getSubject_feed() {
        return subject_feed;
    }

    public void setSubject_feed(String subject_feed) {
        this.subject_feed = subject_feed;
    }

    public Date getDate_feed() {
        return date_feed;
    }

    public void setDate_feed(Date date_feed) {
        this.date_feed = date_feed;
    }

    public String getName_feed() {
        return name_feed;
    }

    public void setName_feed(String name_feed) {
        this.name_feed = name_feed;
    }

    public boolean isIs_processed() {
        return is_processed;
    }

    public void setIs_processed(boolean is_processed) {
        this.is_processed = is_processed;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
} 