package com.mindshield.ui;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SettingsController {
    @FXML private TextField newPersonaName;
    @FXML private PasswordField newPassword;

    @FXML 
    private void saveSettings() { 
        System.out.println("Ayarlar kaydedildi: " + newPersonaName.getText()); 
    }
}