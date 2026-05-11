package com.mindshield.ui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.models.BaseUser;
import com.mindshield.models.StandardUser;
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

    @FXML
    private TextField regUser;
    @FXML
    private PasswordField regPass;
    @FXML
    private RadioButton rbCounselor;
    @FXML
    private RadioButton rbClient;
    @FXML
    private Label lblError;
    @FXML
    private VBox expertiseBox;
    @FXML
    private ComboBox<CounselorExpertise> expertiseCombo;

    @FXML
    public void initialize() {

        if (expertiseCombo != null) {
            expertiseCombo.setItems(
                    FXCollections.observableArrayList(
                            CounselorExpertise.values()));

            expertiseCombo.getSelectionModel().selectFirst();
        }

        Runnable syncExpertiseBox = () -> {

            boolean counselor = rbCounselor != null &&
                    rbCounselor.isSelected();

            if (expertiseBox != null) {
                expertiseBox.setVisible(counselor);
                expertiseBox.setManaged(counselor);
            }

            if (lblError != null) {
                lblError.setText("");
            }
        };

        if (rbCounselor != null) {

            rbCounselor.selectedProperty().addListener(
                    (obs, oldVal, newVal) -> {

                        if (regUser != null) {

                            regUser.setPromptText(
                                    Boolean.TRUE.equals(newVal)
                                            ? "Ad ve soyad"
                                            : "Persona Adi");
                        }

                        syncExpertiseBox.run();
                    });
        }

        if (rbClient != null) {

            rbClient.selectedProperty().addListener(
                    (obs, o, n) -> syncExpertiseBox.run());
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

        UserRole role = rbCounselor.isSelected()
                ? UserRole.PENDING_COUNSELOR
                : UserRole.CLIENT;

        CounselorExpertise expertise = expertiseCombo != null
                ? expertiseCombo.getSelectionModel().getSelectedItem()
                : null;

        if (role == UserRole.PENDING_COUNSELOR && expertise == null) {

            showError("Danisman kaydinda uzmanlik alani secmelisiniz.");
            return;
        }

        String id = java.util.UUID.randomUUID().toString();

        try (
                Connection conn = DatabaseConnection.getConnection()) {

            // username var mı kontrol et
            PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT username FROM users WHERE username = ?");

            checkStmt.setString(1, persona);

            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {

                showError("Bu kullanici adi zaten kullaniliyor.");
                return;
            }

            // insert işlemi
            PreparedStatement insertStmt = conn.prepareStatement("""
                        INSERT INTO users (
                            id,
                            username,
                            password,
                            role,
                            profession
                        )
                        VALUES (?, ?, ?, ?, ?)
                    """);

            insertStmt.setString(1, id);
            insertStmt.setString(2, persona);
            insertStmt.setString(3, pass);
            insertStmt.setString(4, role.name());

            if (role == UserRole.PENDING_COUNSELOR) {
                insertStmt.setString(5, expertise.getDisplayName());
            } else {
                insertStmt.setNull(5, java.sql.Types.VARCHAR);
            }

            insertStmt.executeUpdate();

            // uygulama içi obje oluştur
            BaseUser newUser;

            if (role == UserRole.PENDING_COUNSELOR) {
                // Danisman basvurusu yapildi, hemen login etme.
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("Kayıt Başarılı");
                alert.setHeaderText(null);
                alert.setContentText("Danışmanlık başvurunuz alınmıştır. Sistem yöneticisi onayladıktan sonra giriş yapabilirsiniz.");
                alert.showAndWait();
                navigateTo("/Login.fxml", "MindShield — Giris");
                return;
            } else {

                newUser = new StandardUser(
                        id,
                        persona,
                        pass,
                        role);
            }

            DashboardController.setCurrentUser(newUser);

            navigateTo(
                    "/Welcome.fxml",
                    "MindShield — Hos Geldin!");

        } catch (Exception e) {

            com.mindshield.util.AppLog.severe(e);
            showError("Veritabani kayit hatasi.");
        }
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

            Parent root = FXMLLoader.load(
                    getClass().getResource(fxml));

            Stage stage = (Stage) regUser.getScene().getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle(title);

        } catch (IOException e) {

            System.err.println("Sahne gecis hatasi: " + fxml);
            com.mindshield.util.AppLog.severe(e);
        }
    }
}