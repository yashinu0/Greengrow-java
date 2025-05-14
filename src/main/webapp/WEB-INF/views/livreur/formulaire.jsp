<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${livreur == null ? 'Ajouter' : 'Modifier'} un Livreur</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-4">
        <h2>${livreur == null ? 'Ajouter' : 'Modifier'} un Livreur</h2>
        
        <form action="${livreur == null ? 'ajouter-livreur' : 'modifier-livreur'}" method="post" class="needs-validation" novalidate>
            <c:if test="${livreur != null}">
                <input type="hidden" name="id" value="${livreur.id}">
            </c:if>
            
            <div class="mb-3">
                <label for="nomLivreur" class="form-label">Nom</label>
                <input type="text" class="form-control" id="nomLivreur" name="nomLivreur" 
                       value="${livreur.nomLivreur}" required>
                <div class="invalid-feedback">
                    Le nom est obligatoire
                </div>
            </div>
            
            <div class="mb-3">
                <label for="prenomLivreur" class="form-label">Prénom</label>
                <input type="text" class="form-control" id="prenomLivreur" name="prenomLivreur" 
                       value="${livreur.prenomLivreur}" required>
                <div class="invalid-feedback">
                    Le prénom est obligatoire
                </div>
            </div>
            
            <div class="mb-3">
                <label for="numeroLivreur" class="form-label">Numéro (8 chiffres)</label>
                <input type="text" class="form-control" id="numeroLivreur" name="numeroLivreur" 
                       value="${livreur.numeroLivreur}" pattern="[0-9]{8}" required>
                <div class="invalid-feedback">
                    Le numéro doit contenir exactement 8 chiffres
                </div>
            </div>
            
            <div class="mb-3">
                <label for="addresseLivreur" class="form-label">Adresse Email</label>
                <input type="email" class="form-control" id="addresseLivreur" name="addresseLivreur" 
                       value="${livreur.addresseLivreur}" required>
                <div class="invalid-feedback">
                    Veuillez entrer une adresse email valide
                </div>
            </div>
            
            <button type="submit" class="btn btn-primary">${livreur == null ? 'Ajouter' : 'Modifier'}</button>
            <a href="liste-livreurs" class="btn btn-secondary">Annuler</a>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Validation des formulaires Bootstrap
        (function () {
            'use strict'
            var forms = document.querySelectorAll('.needs-validation')
            Array.prototype.slice.call(forms)
                .forEach(function (form) {
                    form.addEventListener('submit', function (event) {
                        if (!form.checkValidity()) {
                            event.preventDefault()
                            event.stopPropagation()
                        }
                        form.classList.add('was-validated')
                    }, false)
                })
        })()
    </script>
</body>
</html>
