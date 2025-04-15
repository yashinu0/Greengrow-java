package Test;

import Entites.utilisateur;
import Services.utilisateurService;
import Utils.MyDB;

import java.util.List;

public class testBD {
    public static void main(String[] args) {
        MyDB db = MyDB.getInstance();
        utilisateur utilisateur = new utilisateur(23,"ilyes", "Doe", "john.doe@example.com", "password123", "admin",
                "123 rue de la ferme", "75001", "0123456789", "Paris", true);
        utilisateurService utilisateurService = new utilisateurService();

        //utilisateurService.add(utilisateur);


        List<utilisateur> users = utilisateurService.find();

        for (utilisateur u : users) {
            System.out.println(u);
            System.out.println();
        }
        utilisateurService.update(utilisateur);



    }
}