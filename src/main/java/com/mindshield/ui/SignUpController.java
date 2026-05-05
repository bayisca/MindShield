package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {

    @FXML private TextField     regUser;
    @FXML private PasswordField regPass;
    @FXML private RadioButton   rbCounselor;
    @FXML private Label         lblError;

    @FXML
    private void handleSignUp() {
        String persona = regUser.getText().trim();
        String pass    = regPass.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Persona adı ve şifre boş bırakılamaz.");
            return;
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