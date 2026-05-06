package com.mindshield.ui;

import java.io.IOException;

import com.mindshield.models.BaseUser;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AdminLoginController {

    @FXML private TextField     personaField;
    @FXML private PasswordField passwordField;
    @FXML private Label         lblError;

    @FXML
    private void handleLogin() {
        String persona = personaField.getText().trim();
        String pass = passwordField.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Persona adı ve şifre zorunludur.");
            return;
        }

        BaseUser user = MainApp.userDatabase.get(persona);
        if (user == null || !user.getPassword().equals(pass)) {
            showError("Bilgiler hatalı.");
            passwordField.clear();
            return;
        }

        if (user.getRole() != UserRole.ADMIN) {
            showError("Bu giriş yalnızca yönetici hesapları içindir.");
            passwordField.clear();
            return;
        }

        DashboardController.setCurrentUser(user);
        openAdminDashboard();
    }

    @FXML
    private void goUserLogin() {
        navigate("/Login.fxml", "MindShield — Giriş");
    }

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
        }
    }

    private void navigate(String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) personaField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/AdminDashboard.fxml"));
            Stage stage = (Stage) personaField.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield — Yönetici");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
