package com.mindshield.ui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    private static UserRole currentUserRole = UserRole.CLIENT; 

    public static void setCurrentUserRole(UserRole role) {
        currentUserRole = role;
    }

    @FXML
    public void initialize() {
        System.out.println("MindShield Dashboard hazır!");
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
                if (currentUserRole == UserRole.CLIENT) {
                    bc.hideWriteButton();
                }
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void showBlog() { loadView("/Blog.fxml"); }
    @FXML private void showMessages() { loadView("/Messages.fxml"); }
    @FXML private void showProfile() { loadView("/Settings.fxml"); }
}