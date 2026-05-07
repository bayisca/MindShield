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
    @FXML private VBox  featureJour;
    @FXML private Label lblWelcomeDescription;
    @FXML private Label lblMsgTitle;
    @FXML private Label lblMsgDesc;

    @FXML
    public void initialize() {
        com.mindshield.models.BaseUser user = DashboardController.getCurrentUser();
        if (user != null) {
            lblWelcomePersona.setText("Hoş Geldin, " + user.getPersona() + "! 🎉");

            boolean isCounselor = user.getRole() == UserRole.COUNSELOR;

            // JourShield is now visible for both roles according to new requirements
            if (featureJour != null) {
                featureJour.setVisible(true);
                featureJour.setManaged(true);
            }

            if (isCounselor) {
                if (lblWelcomeDescription != null) {
                    lblWelcomeDescription.setText("MindShield'a uzman danışman olarak katıldığın için teşekkürler. Aşağıda uzmanlara özel özellikleri keşfet.");
                }
                if (lblMsgTitle != null) {
                    lblMsgTitle.setText("Danışanlarla İletişim");
                }
                if (lblMsgDesc != null) {
                    lblMsgDesc.setText("Danışanların sana ulaştığında mesajlaşma sistemi üzerinden güvenle onlara destek olabilirsin.");
                }
                lblRoleNote.setText("Danışman hesabı — blog yayınlayabilir ve danışanlara destek sağlayabilirsin.");
            } else {
                if (lblWelcomeDescription != null) {
                    lblWelcomeDescription.setText("Personan başarıyla oluşturuldu. MindShield'a katıldığın için teşekkürler. Aşağıda seni bekleyen özellikleri keşfet.");
                }
                if (lblMsgTitle != null) {
                    lblMsgTitle.setText("Anonim Mesajlaşma");
                }
                if (lblMsgDesc != null) {
                    lblMsgDesc.setText("Danışmanlarla gerçek kimliğin gizli kalarak özel görüş ve destek al.");
                }
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
