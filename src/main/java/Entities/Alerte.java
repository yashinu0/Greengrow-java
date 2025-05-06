package Entities;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Alerte {
    private final IntegerProperty id_alerte = new SimpleIntegerProperty();
    private final StringProperty Niveau_urgence_alerte = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> temps_limite_alerte = new SimpleObjectProperty<>();
    private final ObjectProperty<Rendu> rendu = new SimpleObjectProperty<>();

    public Alerte() {}

    public Alerte(String niveau, LocalDateTime temps) {
        setNiveau_urgence_alerte(niveau);
        setTemps_limite_alerte(temps);
    }

    // Getters and setters for all properties
    public int getId_alerte() { return id_alerte.get(); }
    public void setId_alerte(int id) { this.id_alerte.set(id); }
    public IntegerProperty id_alerteProperty() { return id_alerte; }

    public String getNiveau_urgence_alerte() { return Niveau_urgence_alerte.get(); }
    public void setNiveau_urgence_alerte(String niveau) { this.Niveau_urgence_alerte.set(niveau); }
    public StringProperty Niveau_urgence_alerteProperty() { return Niveau_urgence_alerte; }

    public LocalDateTime getTemps_limite_alerte() { return temps_limite_alerte.get(); }
    public void setTemps_limite_alerte(LocalDateTime date) { this.temps_limite_alerte.set(date); }
    public ObjectProperty<LocalDateTime> temps_limite_alerteProperty() { return temps_limite_alerte; }

    public Rendu getRendu() { return rendu.get(); }
    public void setRendu(Rendu rendu) { this.rendu.set(rendu); }
    public ObjectProperty<Rendu> renduProperty() { return rendu; }
}