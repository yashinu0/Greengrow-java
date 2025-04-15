-- Vérifier si la base de données existe
CREATE DATABASE IF NOT EXISTS greengrow;

-- Utiliser la base de données
USE greengrow;

-- Vérifier si la table reclamation existe
CREATE TABLE IF NOT EXISTS reclamation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id INT NOT NULL,
    produit_id INT NOT NULL,
    description_rec TEXT NOT NULL,
    statut_rec VARCHAR(50) NOT NULL,
    date_rec TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message_reclamation TEXT,
    historique_conversations TEXT
);

-- Insérer des données de test
INSERT INTO reclamation (utilisateur_id, produit_id, description_rec, statut_rec, message_reclamation)
VALUES 
(1, 1, 'Première réclamation de test', 'Pending', 'Message initial'),
(2, 2, 'Deuxième réclamation de test', 'In Progress', 'En cours de traitement');

-- Vérifier les données
SELECT * FROM reclamation; 