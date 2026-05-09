package com.mindshield.ui;

import java.io.IOException;
import java.sql.ClientInfoStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.h2.engine.User;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.models.Admin;
import com.mindshield.models.BaseUser;
import com.mindshield.models.StandardUser;
import com.mindshield.models.Counselor;
import com.mindshield.ui.UserRole;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

    @FXML
    private TextField personaField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label lblError;
    @FXML
    private Text textMind;
    @FXML
    private Text textShield;

    private static final String BRAND_TEXT = "MINDSHIELD";

    @FXML
    public void initialize() {
        playBrandTypingAnimation();
    }

    @FXML
    private void handleLogin() {

        String persona = personaField.getText().trim();
        String pass = passwordField.getText();

        if (persona.isEmpty() || pass.isEmpty()) {
            showError("Persona adı ve şifre boş bırakılamaz.");
            return;
        }

        try (
                Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM users WHERE username = ? AND password = ?")) {

            ps.setString(1, persona);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String id = rs.getString("id");
                String username = rs.getString("username");
                String password = rs.getString("password");
                String roleStr = rs.getString("role");
                String profession = rs.getString("profession");

                BaseUser user;

                if ("PENDING_COUNSELOR".equalsIgnoreCase(roleStr)) {
                    showError("Hesabınız yönetici onayında. Lütfen daha sonra tekrar deneyiniz.");
                    return;
                }

                if ("ADMIN".equalsIgnoreCase(roleStr)) {

                    user = new Admin(username, id, password);

                } else if ("COUNSELOR".equalsIgnoreCase(roleStr)) {

                    user = new Counselor(
                            id,
                            username,
                            password,
                            profession);

                } else {

                    user = new StandardUser(
                            id,
                            username,
                            password,
                            UserRole.CLIENT);
                }

                DashboardController.setCurrentUser(user);

                if (user.getRole() == UserRole.ADMIN) {
                    changeScene("/AdminDashboard.fxml", "MindShield — Yönetici");
                } else {
                    switchToDashboard();
                }
                System.out.println("ID: " + user.getId());
System.out.println("PERSONA: " + user.getPersona());
            } else {

                showError("Persona veya şifre hatalı.");
                passwordField.clear();
            }

        } catch (Exception e) {

            e.printStackTrace();
            showError("Veritabanı bağlantı hatası.");
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

        if (textMind == null || textShield == null)
            return;

        textMind.setText("");
        textShield.setText("");

        Timeline timeline = new Timeline();
        long currentDelay = 0;

        String mindStr = "Mind";
        for (int i = 1; i <= mindStr.length(); i++) {
            final int end = i;
            currentDelay += 250L;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(currentDelay),
                    e -> textMind.setText(mindStr.substring(0, end))
            ));
        }

        String shieldStr = "Shield";
        for (int i = 1; i <= shieldStr.length(); i++) {
            final int end = i;
            currentDelay += 250L;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(currentDelay),
                    e -> textShield.setText(shieldStr.substring(0, end))
            ));
        }

        timeline.play();
    }
}