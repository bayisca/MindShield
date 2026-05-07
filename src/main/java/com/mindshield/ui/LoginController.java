package com.mindshield.ui;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML private TextField     personaField;
    @FXML private PasswordField passwordField;
    @FXML private Label         lblError;
    @FXML private javafx.scene.text.Text textMind;
    @FXML private javafx.scene.text.Text textShield;

    private static final String MIND_TEXT = "MIND";
    private static final String SHIELD_TEXT = "SHIELD";

    @FXML
    public void initialize() {
        playBrandTypingAnimation();
    }

    @FXML
    private void handleLogin() {
        String persona = personaField.getText().trim();
        String pass    = passwordField.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Persona adı ve şifre boş bırakılamaz.");
            return;
        }

        com.mindshield.models.BaseUser user = MainApp.userDatabase.get(persona);

        if (user != null && user.getPassword().equals(pass)) {
            DashboardController.setCurrentUser(user);
            if (user.getRole() == UserRole.ADMIN) {
                changeScene("/AdminDashboard.fxml", "MindShield — Yönetici");
            } else {
                switchToDashboard();
            }
        } else {
            showError("Persona veya şifre hatalı. Tekrar dene.");
            passwordField.clear();
        }
    }

    @FXML
    private void goToSignUp() {
        changeScene("/SignUp.fxml", "MindShield — Kayıt Ol");
    }

    @FXML
    private void openAdminLogin() {
        changeScene("/AdminLogin.fxml", "MindShield — Yönetici Girişi");
    }

    private void showError(String msg) {
        if (lblError != null) {
            lblError.setText(msg);
        }
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
        changeScene("/Dashboard.fxml", "MindShield — Dashboard");
    }

    private void playBrandTypingAnimation() {
        if (textMind == null || textShield == null) return;
        textMind.setText("");
        textShield.setText("");

        Timeline timeline = new Timeline();
        long currentDelay = 0;
        
        for (int i = 1; i <= MIND_TEXT.length(); i++) {
            final int end = i;
            currentDelay += 350L;
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(currentDelay),
                            e -> textMind.setText(MIND_TEXT.substring(0, end)))
            );
        }

        for (int i = 1; i <= SHIELD_TEXT.length(); i++) {
            final int end = i;
            currentDelay += 400L;
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(currentDelay),
                            e -> textShield.setText(SHIELD_TEXT.substring(0, end)))
            );
        }
        
        timeline.play();
    }
}