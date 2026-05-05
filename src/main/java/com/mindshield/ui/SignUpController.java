package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SignUpController {
    @FXML private TextField regUser;
    @FXML private PasswordField regPass;
    @FXML private TextField regExpertise;
    @FXML private RadioButton rbCounselor;

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
        }
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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) regUser.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield - Giriş");
        } catch (IOException e) {
            System.err.println("Login.fxml yüklenirken hata oluştu!");
            e.printStackTrace();
        }
    }
}