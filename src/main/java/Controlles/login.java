    package Controlles;

    import java.io.IOException;
    import java.net.URL;
    import java.util.Random;
    import java.util.ResourceBundle;

    import Entites.utilisateur;
    import Services.utilisateurService;
    import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
    import javafx.application.Platform;
    import javafx.fxml.FXML;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.Alert;
    import javafx.scene.control.PasswordField;
    import javafx.scene.control.TextField;
    import javafx.stage.Stage;

    import Utils.sendEmail;


    import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
    import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
    import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
    import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
    import com.google.api.client.json.JsonFactory;
    import com.google.api.client.json.jackson2.JacksonFactory;
    import com.google.api.client.util.store.MemoryDataStoreFactory;
    import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
    import com.google.api.client.auth.oauth2.Credential;
    import com.google.api.services.oauth2.Oauth2;
    import com.google.api.services.oauth2.model.Userinfo;

    import java.io.InputStreamReader;
    import java.util.Arrays;
    import java.util.UUID;


    public class login {

        @FXML
        private ResourceBundle resources;

        @FXML
        private URL location;

        @FXML
        private TextField emailfx;

        @FXML
        private PasswordField mdpFx;

        @FXML
        void initialize() {
        }

        @FXML
        void annulerfx(ActionEvent event) {
        }

        @FXML
        void loginFx(ActionEvent event) {
            String email = emailfx.getText();
            String password = mdpFx.getText();

            utilisateurService us = new utilisateurService();
            utilisateur u = us.login(email, password);

            if (u != null) {
                if (u.getRole_user().equals("ROLE_CLIENT")) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/FrontEnd.fxml"));
                    try {
                        Parent root = loader.load();
                        FrontEnd controller = loader.getController();
                        controller.setCurrentUserId(u.getId_user());
                        controller.setNomfx(u.getNom_user());
                        controller.setPrenomfx(u.getPrenom_user());
                        controller.setEmailfx(u.getEmail_user());

                        controller.nomLabel.setText(u.getNom_user());
                        controller.prenomLabel.setText(u.getPrenom_user());



                        Stage stage = (Stage) emailfx.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Mon profil");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/DashboardTemplate.fxml"));
                    try {
                        Parent root = loader.load();
                        DashboardController controller = loader.getController();
                        Stage stage = (Stage) emailfx.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Dashboard Administrateur");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de connexion");
                alert.setHeaderText("Identifiants incorrects");
                alert.setContentText("Vérifiez votre email et mot de passe.");
                alert.show();
            }
        }

        @FXML
        void ajouterFx(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
                Stage stage = (Stage) emailfx.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @FXML
        void oubliefx(ActionEvent event) {
            String email = emailfx.getText().trim();

            if (email.isEmpty()) {
                showAlert("Erreur", "Veuillez entrer votre email");
                return;
            }

            utilisateurService us = new utilisateurService();
            utilisateur user = us.findByEmail(email);

            if (user == null) {
                showAlert("Erreur", "Aucun compte associé à cet email");
                return;
            }

            String verificationCode = generateVerificationCode();
            sendEmail send = new sendEmail();
            if (send.sendVerificationEmail(email, verificationCode)) {
                System.out.println("Code envoyé à l'utilisateur : " + verificationCode);
            }
            System.out.println(verificationCode);

            loadCodeVerificationScreen(email, verificationCode);
        }

        private String generateVerificationCode() {
            return String.format("%06d", new Random().nextInt(999999));
        }

        private void loadCodeVerificationScreen(String email, String code) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/CodeVerification.fxml"));
                Parent root = loader.load();

                CodeVerification controller = loader.getController();
                controller.setUserEmailAndCode(email, code);

                Stage stage = (Stage) emailfx.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void showAlert(String title, String content) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        }

        @FXML
        void oubliefx1(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/ForgotPassword.fxml"));
                Stage stage = (Stage) emailfx.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @FXML
        void FrontEndFx(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/FrontEnd.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        @FXML
        void InscFrontFx(ActionEvent event) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/AjouterUtilisateur.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Connexion");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        public void loginWithGoogle() {
            try {
                // 1. Configuration du flux OAuth2
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory,
                        new InputStreamReader(getClass().getResourceAsStream("/client_secret.json")));

                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        jsonFactory,
                        clientSecrets,
                        Arrays.asList(
                                "https://www.googleapis.com/auth/userinfo.profile",
                                "https://www.googleapis.com/auth/userinfo.email"))
                        .setDataStoreFactory(new MemoryDataStoreFactory())
                        .setAccessType("offline")
                        .build();

                // 2. Lancement du serveur local pour la réception du code
                LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8890).build();
                // 3. Exécution dans un thread séparé pour ne pas bloquer l'UI
                new Thread(() -> {
                    try {
                        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

                        // 4. Récupération des informations utilisateur
                        Oauth2 oauth2 = new Oauth2.Builder(
                                GoogleNetHttpTransport.newTrustedTransport(),
                                jsonFactory,
                                credential)
                                .setApplicationName("Green Grow")
                                .build();

                        Userinfo userInfo = oauth2.userinfo().get().execute();

                        // 5. Traitement dans le thread UI
                        Platform.runLater(() -> {
                            handleGoogleUser(userInfo);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Erreur Google", "Échec de l'authentification : " + e.getMessage());
                        });
                    }
                }).start();

            } catch (Exception e) {
                showAlert("Erreur Configuration", "Erreur d'initialisation Google : " + e.getMessage());
            }
        }

        private void handleGoogleUser(Userinfo userInfo) {
            try {
                utilisateurService us = new utilisateurService();
                utilisateur user = us.findByEmail(userInfo.getEmail());

                // Création du compte si inexistant
                if (user == null) {
                    user = new utilisateur();
                    user.setNom_user(userInfo.getFamilyName());
                    user.setPrenom_user(userInfo.getGivenName());
                    user.setEmail_user(userInfo.getEmail());
                    user.setRole_user("ROLE_CLIENT");
                    user.setMot_de_passe_user(generateRandomPassword()); // Méthode à implémenter
                    us.addAndReturnId(user);
                }

                // Redirection selon le rôle
                if (user.getRole_user().equals("ROLE_CLIENT")) {
                    loadFXML("/FrontEnd.fxml", user);
                } else {
                    loadFXML("/ListUser.fxml", user);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur de traitement : " + e.getMessage());
            }
        }

        private void loadFXML(String fxmlPath, utilisateur user) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                if (fxmlPath.equals("/FrontEnd.fxml")) {
                    FrontEnd controller = loader.getController();
                    controller.setCurrentUserId(user.getId_user());
                    controller.nomLabel.setText(user.getNom_user());
                    controller.prenomLabel.setText(user.getPrenom_user());

                    // Mettre à jour l'état de connexion
                    controller.loginButton.setVisible(false);
                    controller.plusButton.setVisible(true);


                }

                Stage stage = (Stage) emailfx.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                showAlert("Erreur", "Échec du chargement de l'interface");
            }
        }

        private String generateRandomPassword() {
            // Implémentez une génération de mot de passe sécurisé
            return UUID.randomUUID().toString().substring(0, 12);
        }
    }


