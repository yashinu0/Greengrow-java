package Entites;

import javafx.beans.property.*;

public class Produit {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty idCategories = new SimpleIntegerProperty();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final StringProperty nomProduit = new SimpleStringProperty();
    private final StringProperty descriptionProduit = new SimpleStringProperty();
    private final IntegerProperty prixProduit = new SimpleIntegerProperty();
    private final StringProperty disponibilteProduit = new SimpleStringProperty();
    private final StringProperty imageProduit = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty();

    // Getters for properties (for JavaFX binding)
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty idCategoriesProperty() { return idCategories; }
    public IntegerProperty quantiteProperty() { return quantite; }
    public StringProperty nomProduitProperty() { return nomProduit; }
    public StringProperty descriptionProduitProperty() { return descriptionProduit; }
    public IntegerProperty prixProduitProperty() { return prixProduit; }
    public StringProperty disponibilteProduitProperty() { return disponibilteProduit; }
    public StringProperty imageProduitProperty() { return imageProduit; }
    public StringProperty locationProperty() { return location; }
    public DoubleProperty ratingProperty() { return rating; }

    // Regular getters and setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getIdCategories() { return idCategories.get(); }
    public void setIdCategories(int idCategories) { this.idCategories.set(idCategories); }

    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }

    public String getNomProduit() { return nomProduit.get(); }
    public void setNomProduit(String nomProduit) { this.nomProduit.set(nomProduit); }

    public String getDescriptionProduit() { return descriptionProduit.get(); }
    public void setDescriptionProduit(String descriptionProduit) { this.descriptionProduit.set(descriptionProduit); }

    public int getPrixProduit() { return prixProduit.get(); }
    public void setPrixProduit(int prixProduit) { this.prixProduit.set(prixProduit); }

    public String getDisponibilteProduit() { return disponibilteProduit.get(); }
    public void setDisponibilteProduit(String disponibilteProduit) { this.disponibilteProduit.set(disponibilteProduit); }

    public String getImageProduit() { return imageProduit.get(); }
    public void setImageProduit(String imageProduit) { this.imageProduit.set(imageProduit); }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }

    public double getRating() { return rating.get(); }
    public void setRating(double rating) { this.rating.set(rating); }
}