package Controlles;

import Utils.MyDB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatistiquesController implements Initializable {

    @FXML
    private ComboBox<String> periodeComboBox;

    @FXML
    private ComboBox<String> typeGraphiqueComboBox;

    @FXML
    private ComboBox<String> typeStatistiqueComboBox;

    @FXML
    private StackPane graphiqueContainer;

    @FXML
    private BarChart<String, Number> livreurChart;
    @FXML
    private PieChart commandeChart;
    @FXML
    private Label totalLivreursLabel;
    @FXML
    private Label totalCommandesLabel;

    private Connection connection;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        connection = MyDB.getInstance().getCon();
        periodeComboBox.setItems(FXCollections.observableArrayList(
            "Par jour", "Par mois"
        ));
        periodeComboBox.setValue("Par jour");

        typeGraphiqueComboBox.setItems(FXCollections.observableArrayList(
            "Ligne", "Barre", "Zone"
        ));
        typeGraphiqueComboBox.setValue("Ligne");

        typeStatistiqueComboBox.setItems(FXCollections.observableArrayList(
            "Par montant", "Par statut"
        ));
        typeStatistiqueComboBox.setValue("Par montant");

        loadStatistiques();
    }

    private void loadStatistiques() {
        try {
            // Statistiques des livreurs
            String livreurSql = "SELECT COUNT(*) as total FROM livreur";
            try (PreparedStatement stmt = connection.prepareStatement(livreurSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalLivreurs = rs.getInt("total");
                    totalLivreursLabel.setText("Total Livreurs: " + totalLivreurs);
                }
            }

            // Statistiques des commandes
            String commandeSql = "SELECT COUNT(*) as total FROM commande";
            try (PreparedStatement stmt = connection.prepareStatement(commandeSql);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int totalCommandes = rs.getInt("total");
                    totalCommandesLabel.setText("Total Commandes: " + totalCommandes);
                }
            }

            // Données pour le graphique des livreurs
            XYChart.Series<String, Number> livreurSeries = new XYChart.Series<>();
            livreurSeries.setName("Livreurs par région");
            
            String regionSql = "SELECT addresse_livreur, COUNT(*) as count FROM livreur GROUP BY addresse_livreur";
            try (PreparedStatement stmt = connection.prepareStatement(regionSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livreurSeries.getData().add(new XYChart.Data<>(
                        rs.getString("addresse_livreur"),
                        rs.getInt("count")
                    ));
                }
            }
            livreurChart.getData().add(livreurSeries);

            // Données pour le graphique des commandes
            Map<String, Integer> commandeStats = new HashMap<>();
            String commandeStatusSql = "SELECT status, COUNT(*) as count FROM commande GROUP BY status";
            try (PreparedStatement stmt = connection.prepareStatement(commandeStatusSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    commandeStats.put(
                        rs.getString("status"),
                        rs.getInt("count")
                    );
                }
            }

            commandeStats.forEach((status, count) -> {
                PieChart.Data slice = new PieChart.Data(status, count);
                commandeChart.getData().add(slice);
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleActualiser() {
        String periode = periodeComboBox.getValue();
        String typeGraphique = typeGraphiqueComboBox.getValue();
        String typeStatistique = typeStatistiqueComboBox.getValue();

        if ("Par montant".equals(typeStatistique)) {
            Map<String, Double> donnees = chargerDonneesMontant(periode);
            afficherGraphique(donnees, typeGraphique, periode, typeStatistique);
        } else {
            Map<String, Map<String, Double>> donnees = chargerDonneesStatut(periode);
            afficherGraphiqueParStatut(donnees, typeGraphique, periode);
        }
    }

    private Map<String, Double> chargerDonneesMontant(String periode) {
        Map<String, Double> donnees = new LinkedHashMap<>();
        String sql;

        if ("Par jour".equals(periode)) {
            sql = "SELECT DATE(date_commande) as date, SUM(prixtotal_commande) as total " +
                  "FROM commande " +
                  "GROUP BY DATE(date_commande) " +
                  "ORDER BY date";
        } else {
            sql = "SELECT DATE_FORMAT(date_commande, '%Y-%m') as mois, SUM(prixtotal_commande) as total " +
                  "FROM commande " +
                  "GROUP BY DATE_FORMAT(date_commande, '%Y-%m') " +
                  "ORDER BY mois";
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString(1);
                double total = rs.getDouble("total");
                donnees.put(date, total);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return donnees;
    }

    private Map<String, Map<String, Double>> chargerDonneesStatut(String periode) {
        Map<String, Map<String, Double>> donnees = new LinkedHashMap<>();
        String sql;

        if ("Par jour".equals(periode)) {
            sql = "SELECT DATE(date_commande) as date, statue_commande, COUNT(*) as nombre " +
                  "FROM commande " +
                  "GROUP BY DATE(date_commande), statue_commande " +
                  "ORDER BY date, statue_commande";
        } else {
            sql = "SELECT DATE_FORMAT(date_commande, '%Y-%m') as mois, statue_commande, COUNT(*) as nombre " +
                  "FROM commande " +
                  "GROUP BY DATE_FORMAT(date_commande, '%Y-%m'), statue_commande " +
                  "ORDER BY mois, statue_commande";
        }

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString(1);
                String statut = rs.getString("statue_commande");
                double nombre = rs.getDouble("nombre");

                donnees.computeIfAbsent(date, k -> new LinkedHashMap<>());
                donnees.get(date).put(statut, nombre);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return donnees;
    }

    private void afficherGraphique(Map<String, Double> donnees, String typeGraphique, String periode, String typeStatistique) {
        graphiqueContainer.getChildren().clear();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel(periode.equals("Par jour") ? "Date" : "Mois");
        yAxis.setLabel("Par montant".equals(typeStatistique) ? "Montant total (€)" : "Nombre de commandes");

        XYChart<String, Number> chart = null;

        switch (typeGraphique) {
            case "Ligne":
                LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Évolution des commandes " + periode.toLowerCase());
                XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
                lineSeries.setName("Montant total");
                donnees.forEach((date, montant) -> 
                    lineSeries.getData().add(new XYChart.Data<>(date, montant))
                );
                lineChart.getData().add(lineSeries);
                chart = lineChart;
                break;

            case "Barre":
                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                barChart.setTitle("Évolution des commandes " + periode.toLowerCase());
                XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
                barSeries.setName("Montant total");
                donnees.forEach((date, montant) -> 
                    barSeries.getData().add(new XYChart.Data<>(date, montant))
                );
                barChart.getData().add(barSeries);
                chart = barChart;
                break;

            case "Zone":
                AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
                areaChart.setTitle("Évolution des commandes " + periode.toLowerCase());
                XYChart.Series<String, Number> areaSeries = new XYChart.Series<>();
                areaSeries.setName("Montant total");
                donnees.forEach((date, montant) -> 
                    areaSeries.getData().add(new XYChart.Data<>(date, montant))
                );
                areaChart.getData().add(areaSeries);
                chart = areaChart;
                break;
        }

        if (chart != null) {
            chart.setAnimated(true);
            graphiqueContainer.getChildren().add(chart);
        }
    }

    private void afficherGraphiqueParStatut(Map<String, Map<String, Double>> donnees, String typeGraphique, String periode) {
        graphiqueContainer.getChildren().clear();

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel(periode.equals("Par jour") ? "Date" : "Mois");
        yAxis.setLabel("Nombre de commandes");

        XYChart<String, Number> chart = null;

        // Créer une série pour chaque statut
        Set<String> statuts = new HashSet<>();
        donnees.values().forEach(statusMap -> statuts.addAll(statusMap.keySet()));

        switch (typeGraphique) {
            case "Ligne":
                LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
                lineChart.setTitle("Évolution des commandes par statut " + periode.toLowerCase());
                for (String statut : statuts) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(statut);
                    donnees.forEach((date, statusMap) -> 
                        series.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault(statut, 0.0)))
                    );
                    lineChart.getData().add(series);
                }
                chart = lineChart;
                break;

            case "Barre":
                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                barChart.setTitle("Évolution des commandes par statut " + periode.toLowerCase());
                for (String statut : statuts) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(statut);
                    donnees.forEach((date, statusMap) -> 
                        series.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault(statut, 0.0)))
                    );
                    barChart.getData().add(series);
                }
                chart = barChart;
                break;

            case "Zone":
                AreaChart<String, Number> areaChart = new AreaChart<>(xAxis, yAxis);
                areaChart.setTitle("Évolution des commandes par statut " + periode.toLowerCase());
                for (String statut : statuts) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(statut);
                    donnees.forEach((date, statusMap) -> 
                        series.getData().add(new XYChart.Data<>(date, statusMap.getOrDefault(statut, 0.0)))
                    );
                    areaChart.getData().add(series);
                }
                chart = areaChart;
                break;
        }

        if (chart != null) {
            chart.setAnimated(true);
            graphiqueContainer.getChildren().add(chart);
        }
    }
}
