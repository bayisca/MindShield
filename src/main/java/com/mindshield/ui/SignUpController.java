package com.mindshield.ui;

import java.io.IOException;

import com.mindshield.models.CounselorExpertise;
import com.mindshield.util.PasswordRules;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SignUpController {

    @FXML private TextField     regUser;
    @FXML private PasswordField regPass;
    @FXML private RadioButton   rbCounselor;
    @FXML private RadioButton   rbClient;
    @FXML private Label         lblError;
    @FXML private VBox          expertiseBox;
    @FXML private ComboBox<CounselorExpertise> expertiseCombo;

    @FXML
    public void initialize() {
        if (expertiseCombo != null) {
            expertiseCombo.setItems(FXCollections.observableArrayList(CounselorExpertise.values()));
            expertiseCombo.getSelectionModel().selectFirst();
        }

        Runnable syncExpertiseBox = () -> {
            boolean counselor = rbCounselor != null && rbCounselor.isSelected();
            if (expertiseBox != null) {
                expertiseBox.setVisible(counselor);
                expertiseBox.setManaged(counselor);
            }
            if (lblError != null) {
                lblError.setText("");
            }
        };

        if (rbCounselor != null) {
            rbCounselor.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (regUser != null) {
                    regUser.setPromptText(Boolean.TRUE.equals(newVal) ? "Ad ve soyad (gorunecek isim)" : "Persona Adi");
                }
                syncExpertiseBox.run();
            });
        }
        if (rbClient != null) {
            rbClient.selectedProperty().addListener((obs, o, n) -> syncExpertiseBox.run());
        }
        syncExpertiseBox.run();
    }

    @FXML
    private void handleSignUp() {
        String persona = regUser.getText().trim();
        String pass = regPass.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Kullanici adi ve sifre bos birakilamaz.");
            return;
        }

        String passErr = PasswordRules.validate(pass);
        if (passErr != null) {
            showError(passErr);
            return;
        }

        if (MainApp.userDatabase.containsKey(persona)) {
            showError("Bu kullanici adi zaten kullaniliyor. Baska bir ad deneyin.");
            return;
        }

        UserRole role = rbCounselor.isSelected() ? UserRole.COUNSELOR : UserRole.CLIENT;
        String id = java.util.UUID.randomUUID().toString();

        CounselorExpertise expertise = expertiseCombo != null
                ? expertiseCombo.getSelectionModel().getSelectedItem()
                : null;

        if (role == UserRole.COUNSELOR) {
            if (expertise == null) {
                showError("Danisman kaydinda uzmanlik alani secmelisiniz.");
                return;
            }
        }

        com.mindshield.models.BaseUser newUser;
        if (role == UserRole.COUNSELOR) {
            newUser = new com.mindshield.models.Counselor(persona, id, pass, expertise.getDisplayName());
        } else {
            newUser = new com.mindshield.models.StandardUser(persona, id, pass, role);
        }

        MainApp.userDatabase.put(persona, newUser);

        DashboardController.setCurrentUser(newUser);
        navigateTo("/Welcome.fxml", "MindShield — Hos Geldin!");
    }

    @FXML
    private void goToLogin() {
        navigateTo("/Login.fxml", "MindShield — Giris");
    }

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
        }
    }

    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) regUser.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            System.err.println("Sahne gecis hatasi: " + fxml);
            e.printStackTrace();
        }
    }
}
