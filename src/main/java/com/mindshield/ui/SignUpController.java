package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
<<<<<<< HEAD
import javafx.scene.Scene;
import javafx.scene.control.Alert;
=======
import javafx.scene.control.Label;
>>>>>>> 286b22c3a7cd0add4fcd2855c1bdf0fbdd8872ee
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {

    @FXML private TextField     regUser;
    @FXML private PasswordField regPass;
<<<<<<< HEAD
    @FXML private TextField regExpertise;
    @FXML private RadioButton rbCounselor;
=======
    @FXML private RadioButton   rbCounselor;
    @FXML private Label         lblError;
>>>>>>> 286b22c3a7cd0add4fcd2855c1bdf0fbdd8872ee

    @FXML
    public void initialize() {
        regExpertise.setVisible(false);
        regExpertise.setManaged(false);
        
        rbCounselor.selectedProperty().addListener((obs, oldVal, newVal) -> {
            regExpertise.setVisible(newVal);
            regExpertise.setManaged(newVal);
            if (newVal) {
                regUser.setPromptText("Gerçek Adınız");
            } else {
                regUser.setPromptText("Persona Adı");
            }
        });
    }

    @FXML
    private void handleSignUp() {
<<<<<<< HEAD
        String persona = regUser.getText();
        String pass = regPass.getText();
        String expertise = regExpertise.getText();
        UserRole role = rbCounselor.isSelected() ? UserRole.COUNSELOR : UserRole.CLIENT;

        try {
            MainApp.userService.registerUser(persona, pass, role, expertise);
            showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Kayıt işlemi başarıyla tamamlandı!");
            goToLogin();
        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.WARNING, "Kayıt Hatası", e.getMessage());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Sistem Hatası", "Beklenmeyen bir hata oluştu: " + e.getMessage());
=======
        String persona = regUser.getText().trim();
        String pass    = regPass.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Persona adı ve şifre boş bırakılamaz.");
            return;
>>>>>>> 286b22c3a7cd0add4fcd2855c1bdf0fbdd8872ee
        }

        if (MainApp.userDatabase.containsKey(persona)) {
            showError("Bu persona adı zaten kullanılıyor. Farklı bir ad dene.");
            return;
        }

        if (pass.length() < 4) {
            showError("Şifre en az 4 karakter olmalıdır.");
            return;
        }

        UserRole role = rbCounselor.isSelected() ? UserRole.COUNSELOR : UserRole.CLIENT;
        String   id   = java.util.UUID.randomUUID().toString();

        com.mindshield.models.BaseUser newUser;
        if (role == UserRole.COUNSELOR) {
            newUser = new com.mindshield.models.Counselor(persona, id, pass, "Genel Psikoloji");
        } else {
            newUser = new com.mindshield.models.StandardUser(persona, id, pass, role);
        }

        MainApp.userDatabase.put(persona, newUser);
        System.out.println("Yeni Persona: " + persona + " (" + role + ")");

        // Log them in and show the Welcome panel
        DashboardController.setCurrentUser(newUser);
        navigateTo("/Welcome.fxml", "MindShield — Hoş Geldin!");
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void goToLogin() {
        navigateTo("/Login.fxml", "MindShield — Giriş");
    }

    private void showError(String msg) {
        if (lblError != null) lblError.setText(msg);
    }

    private void navigateTo(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) regUser.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            System.err.println("Sahne geçiş hatası: " + fxml);
            e.printStackTrace();
        }
    }
}