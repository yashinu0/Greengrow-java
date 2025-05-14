package Entities;

import java.time.LocalDate;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Rendu {
    private final IntegerProperty id_rendu = new SimpleIntegerProperty();
    private final StringProperty message_rendu = new SimpleStringProperty();
    private final StringProperty type_rendu = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> Date_envoi_rendu = new SimpleObjectProperty<>();
    private final ObjectProperty<Alerte> alerte = new SimpleObjectProperty<>();

    public Rendu() {}

    public Rendu(String message, String type, LocalDate date) {
        setMessage_rendu(message);
        setType_rendu(type);
        setDate_envoi_rendu(date);
    }

    // Getters and setters for all properties
    public int getId_rendu() { return id_rendu.get(); }
    public void setId_rendu(int id) { this.id_rendu.set(id); }
    public IntegerProperty id_renduProperty() { return id_rendu; }

    public String getMessage_rendu() { return message_rendu.get(); }
    public void setMessage_rendu(String message) { this.message_rendu.set(message); }
    public StringProperty message_renduProperty() { return message_rendu; }

    public String getType_rendu() { return type_rendu.get(); }
    public void setType_rendu(String type) { this.type_rendu.set(type); }
    public StringProperty type_renduProperty() { return type_rendu; }

    public LocalDate getDate_envoi_rendu() { return Date_envoi_rendu.get(); }
    public void setDate_envoi_rendu(LocalDate date) { this.Date_envoi_rendu.set(date); }
    public ObjectProperty<LocalDate> Date_envoi_renduProperty() { return Date_envoi_rendu; }

    public Alerte getAlerte() { return alerte.get(); }
    public void setAlerte(Alerte alerte) { this.alerte.set(alerte); }
    public ObjectProperty<Alerte> alerteProperty() { return alerte; }
}