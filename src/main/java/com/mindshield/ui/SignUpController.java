package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private RadioButton rbCounselor;

    @FXML
    private void handleSignUp() {
        String persona = regUser.getText();
        String pass = regPass.getText();
        UserRole role = rbCounselor.isSelected() ? UserRole.COUNSELOR : UserRole.CLIENT;

        if (!persona.isEmpty() && !pass.isEmpty()) {
            // BaseUser modelini kullanarak kaydet
            String id = java.util.UUID.randomUUID().toString();
            com.mindshield.models.BaseUser newUser;
            
            if (role == UserRole.COUNSELOR) {
                newUser = new com.mindshield.models.Counselor(persona, id, pass, "Awaiting Approval");
            } else {
                newUser = new com.mindshield.models.StandardUser(persona, id, pass, role);
            }
            
            MainApp.userDatabase.put(persona, newUser);
            System.out.println("Yeni Persona Kaydedildi: " + persona + " (" + role + ")");
            
            goToLogin();
        }
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) regUser.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("MindShield - Giriş");
        } catch (IOException e) {
            System.err.println("Login.fxml yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }
}