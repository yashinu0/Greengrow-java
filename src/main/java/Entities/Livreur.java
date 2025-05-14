package Entities;

import javax.persistence.*;
import javafx.beans.property.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Table(name = "livreur")
public class Livreur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private IntegerProperty id;

    @Column(name = "nom_livreur")
    private StringProperty nomLivreur;

    @Column(name = "prenom_livreur")
    private StringProperty prenomLivreur;

    @Column(name = "numero_livreur")
    private StringProperty numeroLivreur;

    @Column(name = "addresse_livreur")
    private StringProperty addresseLivreur;

    @Column(name = "photo_livreur")
    private StringProperty photoLivreur;

    public Livreur() {
        this.id = new SimpleIntegerProperty();
        this.nomLivreur = new SimpleStringProperty();
        this.prenomLivreur = new SimpleStringProperty();
        this.numeroLivreur = new SimpleStringProperty();
        this.addresseLivreur = new SimpleStringProperty();
        this.photoLivreur = new SimpleStringProperty();
    }

    // Getters pour les propriétés JavaFX
    public IntegerProperty idProperty() { return id; }
    public StringProperty nomLivreurProperty() { return nomLivreur; }
    public StringProperty prenomLivreurProperty() { return prenomLivreur; }
    public StringProperty numeroLivreurProperty() { return numeroLivreur; }
    public StringProperty addresseLivreurProperty() { return addresseLivreur; }
    public StringProperty photoLivreurProperty() { return photoLivreur; }

    // Getters pour les valeurs
    public int getId() { return id.get(); }
    public String getNomLivreur() { return nomLivreur.get(); }
    public String getPrenomLivreur() { return prenomLivreur.get(); }
    public String getNumeroLivreur() { return numeroLivreur.get(); }
    public String getAddresseLivreur() { return addresseLivreur.get(); }
    public String getPhotoLivreur() { return photoLivreur.get(); }

    // Setters
    public void setId(int id) { this.id.set(id); }
    public void setNomLivreur(String nomLivreur) { this.nomLivreur.set(nomLivreur); }
    public void setPrenomLivreur(String prenomLivreur) { this.prenomLivreur.set(prenomLivreur); }
    public void setNumeroLivreur(String numeroLivreur) { this.numeroLivreur.set(numeroLivreur); }
    public void setAddresseLivreur(String addresseLivreur) { this.addresseLivreur.set(addresseLivreur); }
    public void setPhotoLivreur(String photoLivreur) { this.photoLivreur.set(photoLivreur); }
}
