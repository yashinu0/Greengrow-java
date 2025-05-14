package Controlles;

import Entites.utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private utilisateur currentUser;
    private utilisateur tempUser;
    private String verificationCode;
    private long lastActivityTime;

    // Constructeur privé pour empêcher une instanciation directe
    private SessionManager() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Retourner l'instance Singleton
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Getter pour l'utilisateur courant
    public utilisateur getCurrentUser() {
        updateLastActivity();
        return currentUser;
    }

    // Setter pour définir l'utilisateur courant
    public void setCurrentUser(utilisateur user) {
        this.currentUser = user;
        updateLastActivity();
    }

    // Setter pour définir l'utilisateur temporaire
    public void setTempUser(utilisateur user) {
        this.tempUser = user;
        updateLastActivity();
    }

    // Getter pour l'utilisateur temporaire
    public utilisateur getTempUser() {
        return tempUser;
    }

    // Setter pour définir le code de vérification
    public void setVerificationCode(String code) {
        this.verificationCode = code;
        updateLastActivity();
    }

    // Getter pour le code de vérification
    public String getVerificationCode() {
        return verificationCode;
    }

    // Méthode pour nettoyer la session (déconnexion)
    public void clearSession() {
        this.currentUser = null;
        this.tempUser = null;
        this.verificationCode = null;
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Vérifier si un utilisateur est connecté
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    // Vérifier si l'utilisateur a un rôle spécifique
    public boolean hasRole(String role) {
        return isUserLoggedIn() && currentUser.getRole_user().equals(role);
    }

    // Vérifier si la session est expirée (30 minutes d'inactivité)
    public boolean isSessionExpired() {
        if (!isUserLoggedIn()) return true;
        long currentTime = System.currentTimeMillis();
        long sessionTimeout = 30 * 60 * 1000; // 30 minutes
        return (currentTime - lastActivityTime) > sessionTimeout;
    }

    // Mettre à jour le temps de dernière activité
    private void updateLastActivity() {
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Vérifier si l'utilisateur est un administrateur
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    // Vérifier si l'utilisateur est un client
    public boolean isClient() {
        return hasRole("ROLE_CLIENT");
    }

    // Obtenir l'ID de l'utilisateur courant
    public int getCurrentUserId() {
        return isUserLoggedIn() ? currentUser.getId_user() : -1;
    }

    // Obtenir le nom complet de l'utilisateur courant
    public String getCurrentUserFullName() {
        if (!isUserLoggedIn()) return "";
        return currentUser.getNom_user() + " " + currentUser.getPrenom_user();
    }
}