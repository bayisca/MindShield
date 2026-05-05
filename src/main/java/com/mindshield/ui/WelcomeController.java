package com.mindshield.ui;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Shown to a user immediately after they register for the first time.
 * Personalises the welcome message and highlights role-relevant features.
 */
public class WelcomeController {

    @FXML private Label lblWelcomePersona;
    @FXML private Label lblRoleNote;
    @FXML private VBox  featureJour; // JourShield card — hidden for counselors

    @FXML
    public void initialize() {
        com.mindshield.models.BaseUser user = DashboardController.getCurrentUser();
        if (user != null) {
            lblWelcomePersona.setText("Hoş Geldin, " + user.getPersona() + "! 🎉");

            boolean isCounselor = user.getRole() == UserRole.COUNSELOR;

            // JourShield is a client-only feature
            if (featureJour != null) {
                featureJour.setVisible(!isCounselor);
                featureJour.setManaged(!isCounselor);
            }

            if (isCounselor) {
                lblRoleNote.setText("Danışman hesabı — blog yayınlayabilir ve danışanlarla iletişim kurabilirsin.");
            } else {
                lblRoleNote.setText("Danışan hesabı — kimliğin gizli kalır, uzmanlardan destek alabilirsin.");
            }
        }
    }

    @FXML
    private void goToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) lblWelcomePersona.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield — Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
