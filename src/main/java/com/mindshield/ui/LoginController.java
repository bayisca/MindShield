package com.mindshield.ui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField personaField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String persona = personaField.getText();
        String password = passwordField.getText();
        
        System.out.println("Giris denemesi yapildi!");
        System.out.println("Persona: " + persona);
        // Gerçek projede şifreyi asla konsola yazdırmayız kanka, 
        // güvenlik (security) açısından bu çok önemli!
    }
}