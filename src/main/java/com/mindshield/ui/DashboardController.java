package com.mindshield.ui;

import java.io.IOException;

import com.mindshield.services.MeditationPlaybackService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnJourShield;

    private static com.mindshield.models.BaseUser currentUser;
    private static DashboardController instance;

    public static void setCurrentUser(com.mindshield.models.BaseUser user) { // DashboardController'da oturum açan kullanıcıyı saklar
        currentUser = user;
    }

    public static com.mindshield.models.BaseUser getCurrentUser() {
        return currentUser;
    }

    public static DashboardController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;
        System.out.println("MindShield Dashboard hazır!");
        if (btnJourShield != null) {
            UserRole r = currentUser != null ? currentUser.getRole() : null;
            boolean showJour = r == UserRole.CLIENT || r == UserRole.ANONYMOUS;
            btnJourShield.setVisible(showJour);
            btnJourShield.setManaged(showJour);
        }
        showBlog();
    }

    private void loadView(String fxmlFile) {
        try {
            var resource = getClass().getResource(fxmlFile); 
            if (resource == null) {
                System.err.println("HATA: " + fxmlFile + " kaynaklarda bulunamadı!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent view = loader.load();

            if (fxmlFile.equals("/Blog.fxml")) {
                BlogController bc = loader.getController();
                if (currentUser != null && currentUser.getRole() == UserRole.CLIENT) {
                    bc.hideWriteButton();
                }
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showBlogDetail(com.mindshield.models.BlogPost post) { 
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogDetail.fxml"));
            Parent view = loader.load();
            
            BlogDetailController controller = loader.getController();
            controller.setPost(post);
            
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showBlogWrite() {
        loadView("/BlogWrite.fxml");
    }

    public void showBlogEdit(com.mindshield.models.BlogPost post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BlogWrite.fxml"));
            Parent view = loader.load();
            
            BlogWriteController controller = loader.getController();
            controller.setEditPost(post);
            
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML public void showBlog() { loadView("/Blog.fxml"); }
    @FXML private void showMessages() { loadView("/Messages.fxml"); }
    @FXML private void showProfile() { loadView("/Settings.fxml"); }
    @FXML private void showMeditation() { loadView("/Meditation.fxml"); }
    @FXML private void showJourShield() { loadView("/JourShield.fxml"); }

    @FXML
    private void handleLogout() {
        try {
            MeditationPlaybackService.getInstance().stopPlayback();
            setCurrentUser(null);
            Stage stage = (Stage) contentArea.getScene().getWindow();
            Parent loginView = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            
            // Yeni bir sahne oluşturarak pencere boyutunun Login.fxml'e göre ayarlanmasını sağlarız
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}