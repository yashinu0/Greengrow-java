package Entites;

public class Category {
    private int id;
    private String nomCategories;
    private String descriptionCategories;

    public Category() {}

    public Category(int id, String nom, String description) {
        this.id = id;
        this.nomCategories = nom;
        this.descriptionCategories = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNomCategories() { return nomCategories; }
    public void setNomCategories(String nomCategories) { this.nomCategories = nomCategories; }

    public String getDescriptionCategories() { return descriptionCategories; }
    public void setDescriptionCategories(String descriptionCategories) { this.descriptionCategories = descriptionCategories; }
}
