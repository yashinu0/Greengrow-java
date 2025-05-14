<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des Livreurs</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>Liste des Livreurs</h2>
        <a href="ajouter-livreur" class="btn btn-primary mb-3">Ajouter un livreur</a>
        
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nom</th>
                    <th>Prénom</th>
                    <th>Numéro</th>
                    <th>Adresse Email</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${livreurs}" var="livreur">
                    <tr>
                        <td>${livreur.id}</td>
                        <td>${livreur.nomLivreur}</td>
                        <td>${livreur.prenomLivreur}</td>
                        <td>${livreur.numeroLivreur}</td>
                        <td>${livreur.addresseLivreur}</td>
                        <td>
                            <a href="modifier-livreur?id=${livreur.id}" class="btn btn-warning btn-sm">Modifier</a>
                            <a href="supprimer-livreur?id=${livreur.id}" class="btn btn-danger btn-sm" 
                               onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce livreur ?')">Supprimer</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
