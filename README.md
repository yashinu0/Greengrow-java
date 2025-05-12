# Greengrow-java
---

## Utilisation

- **Gestion des livreurs** : Menu "Commandes" > Ajoutez, modifiez, supprimez des livreurs, affectez des photos.
- **Gestion des commandes** : Menu "Commandes" > Ajoutez, modifiez, supprimez des commandes, générez des factures PDF et QR codes.
- **Gestion des produits** : Menu "Produit" > CRUD produits et catégories.
- **Feedbacks & Réclamations** : Menu dédié pour consulter et traiter les retours clients.
- **Supervision** : Suivi des rendus, alertes, et visualisation sur carte.
- **Statistiques** : Accédez aux graphiques dynamiques via le menu dédié.

---

## Personnalisation

- **Styles** : Modifiez `src/main/resources/styles/nature-theme.css` pour adapter l’apparence.
- **Images** : Placez vos images dans `src/main/resources/images/`.
- **Ressources FXML** : Si vous utilisez des vues FXML, placez-les dans `src/main/resources/view/`.

---

## Dépendances principales

- JavaFX
- MySQL Connector/J
- iText (PDF)
- ZXing (QR Code)
- Hibernate (JPA)
- JXMapViewer

---

## Sécurité & Bonnes pratiques

- Authentification sécurisée (à implémenter selon besoin)
- Validation des données côté client et serveur
- Gestion des permissions (rôles admin/utilisateur)
- Protection contre les injections SQL (préparé via JDBC)
- Gestion des erreurs et alertes utilisateur

---

## Contribution

Les contributions sont les bienvenues !  
N'hésitez pas à :
- Fork le projet
- Créer une branche pour votre fonctionnalité
- Commiter vos changements
- Pousser vers la branche
- Ouvrir une Pull Request

---

## Licence

Ce projet est sous licence MIT. Voir le fichier LICENSE pour plus de détails.

---

## Support

Pour toute question ou problème :
- Ouvrir une issue dans le repository GitHub
- Consulter la documentation
- Contacter l'équipe de support

---

## Roadmap

- Intégration de nouveaux moyens de paiement
- Système de fidélité
- Application mobile (Android/iOS)
- API REST complète
- Système de recommandation
- Intégration des réseaux sociaux
