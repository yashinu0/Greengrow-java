package Controlles;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CodeVerification {
    @FXML private TextField codeField;
    @FXML private Label emailLabel;

    private String userEmail;
    private String verificationCode;

    public void setUserEmailAndCode(String email, String code) {
        this.userEmail = email;
        this.verificationCode = code;
        emailLabel.setText("Code envoyé à : " + email);
    }

    @FXML
    void verifyCodeFx(ActionEvent event) {
        String enteredCode = codeField.getText().trim();
        if (enteredCode.equals(verificationCode)) {
            loadPasswordResetScreen();
        } else {
            showError("Code incorrect", "Le code de vérification est incorrect. Essayez à nouveau.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPasswordResetScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ResetPasswordController.fxml"));
            Parent root = loader.load();

            ResetPasswordController controller = loader.getController();
            controller.setUserEmail(userEmail);

            Stage stage = (Stage) codeField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
