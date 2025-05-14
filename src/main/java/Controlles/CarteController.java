package Controlles;

import Entities.Livreur;
import Utils.MyDB;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CarteController implements Initializable {

    @FXML private ComboBox<Livreur> livreurComboBox;
    @FXML private AnchorPane mapContainer;
    @FXML private Label derniereMiseAJourLabel;
    @FXML private Label statusLabel;

    private JXMapViewer mapViewer;
    private final Map<Integer, WaypointPainter<LivreurWaypoint>> waypointPainters = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final double TUNIS_LAT = 36.8065;
    private static final double TUNIS_LON = 10.1815;
    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = MyDB.getInstance().getCon();
        setupMap();
        loadLivreurs();

        // Configurer le ComboBox
        livreurComboBox.setConverter(new LivreurStringConverter());

        // Démarrer la mise à jour automatique
        scheduler.scheduleAtFixedRate(this::actualiserPositions, 0, 30, TimeUnit.SECONDS);
    }

    private void setupMap() {
        mapViewer = new JXMapViewer();

        // Configurer la source des tuiles (OpenStreetMap)
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // Centrer sur Tunis
        GeoPosition tunis = new GeoPosition(TUNIS_LAT, TUNIS_LON);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(tunis);

        // Ajouter la carte à l'interface JavaFX
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(mapViewer);
        mapContainer.getChildren().add(swingNode);
    }

    private void loadLivreurs() {
        try {
            String sql = "SELECT * FROM livreur";
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                Set<LivreurWaypoint> waypoints = new HashSet<>();
                
                while (rs.next()) {
                    Livreur livreur = new Livreur();
                    livreur.setId(rs.getInt("id"));
                    livreur.setNomLivreur(rs.getString("nom_livreur"));
                    livreur.setPrenomLivreur(rs.getString("prenom_livreur"));
                    livreur.setNumeroLivreur(rs.getString("numero_livreur"));
                    livreur.setAddresseLivreur(rs.getString("addresse_livreur"));
                    
                    // Créer un waypoint pour chaque livreur
                    LivreurWaypoint waypoint = new LivreurWaypoint(
                        new GeoPosition(TUNIS_LAT, TUNIS_LON), // Position par défaut
                        livreur
                    );
                    waypoints.add(waypoint);
                }
                
                // Ajouter les waypoints à la carte
                WaypointPainter<LivreurWaypoint> waypointPainter = new WaypointPainter<>();
                waypointPainter.setWaypoints(waypoints);
                mapViewer.setOverlayPainter(waypointPainter);
                
                statusLabel.setText("Carte chargée avec succès");
            }
        } catch (SQLException e) {
            statusLabel.setText("Erreur lors du chargement des livreurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCentrerSurLivreur() {
        Livreur livreur = livreurComboBox.getValue();
        if (livreur != null) {
            // Pour l'instant, on centre sur Tunis car nous n'avons plus les coordonnées GPS
            GeoPosition position = new GeoPosition(TUNIS_LAT, TUNIS_LON);
            mapViewer.setAddressLocation(position);
            mapViewer.setZoom(4);
        }
    }

    @FXML
    private void handleActualiserPositions() {
        actualiserPositions();
    }

    private void actualiserPositions() {
        // Simuler des mouvements aléatoires pour la démonstration
        // Dans une application réelle, ces données viendraient d'un GPS
        Random random = new Random();
        
        for (Livreur livreur : livreurComboBox.getItems()) {
            // Pour l'instant, nous n'avons plus besoin de mettre à jour les positions
            // car nous n'avons plus les coordonnées GPS dans la base de données
        }

        // Mettre à jour la carte
        Platform.runLater(() -> {
            Set<LivreurWaypoint> waypoints = new HashSet<>();
            for (Livreur livreur : livreurComboBox.getItems()) {
                // Pour l'instant, on place tous les livreurs au centre de Tunis
                waypoints.add(new LivreurWaypoint(
                    new GeoPosition(TUNIS_LAT, TUNIS_LON),
                    livreur
                ));
            }

            WaypointPainter<LivreurWaypoint> waypointPainter = new WaypointPainter<>();
            waypointPainter.setWaypoints(waypoints);
            mapViewer.setOverlayPainter(waypointPainter);
            mapViewer.repaint();

            // Mettre à jour le label de dernière mise à jour
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            derniereMiseAJourLabel.setText(formatter.format(java.time.LocalDateTime.now()));
        });
    }

    private static class LivreurWaypoint implements org.jxmapviewer.viewer.Waypoint {
        private final GeoPosition position;
        private final Livreur livreur;

        public LivreurWaypoint(GeoPosition position, Livreur livreur) {
            this.position = position;
            this.livreur = livreur;
        }

        @Override
        public GeoPosition getPosition() {
            return position;
        }

        public Livreur getLivreur() {
            return livreur;
        }
    }

    private static class LivreurStringConverter extends javafx.util.StringConverter<Livreur> {
        @Override
        public String toString(Livreur livreur) {
            return livreur == null ? "" : livreur.getNomLivreur() + " " + livreur.getPrenomLivreur();
        }

        @Override
        public Livreur fromString(String string) {
            return null;
        }
    }
}
