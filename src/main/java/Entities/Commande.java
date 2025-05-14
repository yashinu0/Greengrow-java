package Entities;

import java.time.LocalDateTime;
import javax.persistence.*;

@Entity
@Table(name = "commande")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commande")
    private int idCommande;

    @Column(name = "livreur_commande_id")
    private int livreurCommandeId;

    @Column(name = "statue_commande")
    private String statutCommande;

    @Column(name = "date_commande")
    private LocalDateTime dateCommande;

    @Column(name = "prixtotal_commande")
    private double prixTotalCommande;

    @Column(name = "modepaiement_commande")
    private String modePaiementCommande;

    public Commande() {
    }

    // Getters
    public int getIdCommande() { return idCommande; }
    public int getLivreurCommandeId() { return livreurCommandeId; }
    public String getStatutCommande() { return statutCommande; }
    public LocalDateTime getDateCommande() { return dateCommande; }
    public double getPrixTotalCommande() { return prixTotalCommande; }
    public String getModePaiementCommande() { return modePaiementCommande; }

    // Setters
    public void setIdCommande(int idCommande) { this.idCommande = idCommande; }
    public void setLivreurCommandeId(int livreurCommandeId) { this.livreurCommandeId = livreurCommandeId; }
    public void setStatutCommande(String statutCommande) { this.statutCommande = statutCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    public void setPrixTotalCommande(double prixTotalCommande) { this.prixTotalCommande = prixTotalCommande; }
    public void setModePaiementCommande(String modePaiementCommande) { this.modePaiementCommande = modePaiementCommande; }

}
