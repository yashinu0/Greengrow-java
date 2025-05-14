package servlet;

import Controlles.LivreurController;
import Entities.Livreur;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/livreurs/*"})
public class LivreurServlet extends HttpServlet {
    private LivreurController livreurController;

    @Override
    public void init() throws ServletException {
        super.init();
        livreurController = new LivreurController();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/liste";
        }

        try {
            switch (action) {
                case "/liste":
                    listLivreurs(request, response);
                    break;
                case "/ajouter":
                    showAddForm(request, response);
                    break;
                case "/modifier":
                    showEditForm(request, response);
                    break;
                case "/supprimer":
                    deleteLivreur(request, response);
                    break;
                default:
                    listLivreurs(request, response);
                    break;
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String action = request.getPathInfo();
        try {
            if ("/ajouter".equals(action)) {
                insertLivreur(request, response);
            } else if ("/modifier".equals(action)) {
                updateLivreur(request, response);
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void listLivreurs(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        request.setAttribute("livreurs", livreurController.getAllLivreurs());
        request.getRequestDispatcher("/WEB-INF/views/livreur/liste.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/livreur/formulaire.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        Livreur livreur = livreurController.getLivreurById(id);
        request.setAttribute("livreur", livreur);
        request.getRequestDispatcher("/WEB-INF/views/livreur/formulaire.jsp").forward(request, response);
    }

    private void insertLivreur(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        String nomLivreur = request.getParameter("nom_livreur");
        String prenomLivreur = request.getParameter("prenom_livreur");
        String numeroLivreur = request.getParameter("numero_livreur");
        String addresseLivreur = request.getParameter("addresse_livreur");
        String photoLivreur = request.getParameter("photo_livreur");

        Livreur livreur = new Livreur();
        livreur.setNomLivreur(nomLivreur);
        livreur.setPrenomLivreur(prenomLivreur);
        livreur.setNumeroLivreur(numeroLivreur);
        livreur.setAddresseLivreur(addresseLivreur);
        livreur.setPhotoLivreur(photoLivreur);
        
        livreurController.ajouterLivreur(livreur);
        response.sendRedirect("liste");
    }

    private void updateLivreur(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String nomLivreur = request.getParameter("nom_livreur");
        String prenomLivreur = request.getParameter("prenom_livreur");
        String numeroLivreur = request.getParameter("numero_livreur");
        String addresseLivreur = request.getParameter("addresse_livreur");
        String photoLivreur = request.getParameter("photo_livreur");

        Livreur livreur = new Livreur();
        livreur.setId(id);
        livreur.setNomLivreur(nomLivreur);
        livreur.setPrenomLivreur(prenomLivreur);
        livreur.setNumeroLivreur(numeroLivreur);
        livreur.setAddresseLivreur(addresseLivreur);
        livreur.setPhotoLivreur(photoLivreur);
        
        livreurController.updateLivreur(livreur);
        response.sendRedirect("liste");
    }

    private void deleteLivreur(HttpServletRequest request, HttpServletResponse response) 
            throws SQLException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        livreurController.deleteLivreur(id);
        response.sendRedirect("liste");
    }
}
