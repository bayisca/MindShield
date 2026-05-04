package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField personaField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String persona = personaField.getText();
        String pass = passwordField.getText();

        com.mindshield.models.BaseUser user = MainApp.userDatabase.get(persona);

        if (user != null && user.getPassword().equals(pass)) {
            System.out.println("Giriş başarılı! Hoş geldin: " + persona);
            DashboardController.setCurrentUser(user);
            switchToDashboard();
        } else {
            System.out.println("Hata: Persona veya şifre geçersiz.");
        }
    }

    @FXML
    private void goToSignUp() {
        changeScene("/SignUp.fxml", "MindShield - Kayıt Ol");
    }

    private void changeScene(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) personaField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToDashboard() {
        changeScene("/Dashboard.fxml", "MindShield - Dashboard");
    }
}