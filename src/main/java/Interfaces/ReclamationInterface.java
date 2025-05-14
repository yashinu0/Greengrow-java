package Interfaces;

import Entities.Reclamation;
import java.util.List;

public interface ReclamationInterface {
    void addReclamation(Reclamation reclamation);
    List<Reclamation> getAllReclamations();
    Reclamation getReclamationById(int id);
    void updateReclamation(Reclamation reclamation);
    void deleteReclamation(int id);
    List<Reclamation> getReclamationsByUser(int utilisateur_id);
    List<Reclamation> getReclamationsByProduct(int produit_id);
    void updateReclamationStatus(int id, String newStatus);
}