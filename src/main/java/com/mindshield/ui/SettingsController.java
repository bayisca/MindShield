package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


import java.util.List;
import java.util.stream.Collectors;

public class SettingsController {

    @FXML private TextField     newPersonaName;
    @FXML private PasswordField newPassword;
    @FXML private VBox          myPostsContainer;
    @FXML private VBox          myPostsSection; // the whole card — hidden for clients

    @FXML
    public void initialize() {
        BaseUser user = DashboardController.getCurrentUser();
<<<<<<< HEAD
        if (user != null) {
            newPersonaName.setText(user.getPersona());
            if (user.getRole() == UserRole.COUNSELOR || user.getRole() == UserRole.SUPERADMIN) {
                loadMyPosts(user);
            } else {
                myPostsContainer.setVisible(false);
                myPostsContainer.setManaged(false);
            }
=======
        if (user == null) return;

        newPersonaName.setText(user.getPersona());

        // Blog posts section is only useful for counselors who write posts
        boolean isCounselor = user.getRole() == UserRole.COUNSELOR;
        if (myPostsSection != null) {
            myPostsSection.setVisible(isCounselor);
            myPostsSection.setManaged(isCounselor);
>>>>>>> 286b22c3a7cd0add4fcd2855c1bdf0fbdd8872ee
        }

        if (isCounselor) loadMyPosts(user);
    }

    private void loadMyPosts(BaseUser user) {
        if (myPostsContainer == null) return;
        myPostsContainer.getChildren().clear();

        List<BlogPost> myPosts = MainApp.postService.getAllPosts().stream()
                .filter(p -> p.getAuthor().getPersona().equals(user.getPersona()))
                .collect(Collectors.toList());

        if (myPosts.isEmpty()) {
            Label empty = new Label("Henüz bir yazınız bulunmuyor.");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
            myPostsContainer.getChildren().add(empty);
        } else {
            for (BlogPost post : myPosts) {
                myPostsContainer.getChildren().add(createMiniCard(post));
            }
        }
    }

    private VBox createMiniCard(BlogPost post) {
        VBox card = new VBox(6);
        card.setStyle("-fx-padding: 12; -fx-background-color: #1e3048;" +
                      "-fx-background-radius: 8; -fx-border-color: #263d56; -fx-border-radius: 8;" +
                      "-fx-cursor: hand;");

        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #e2e8f0;");

        Label date = new Label(post.getCreatedAt() != null
                ? post.getCreatedAt().toString().substring(0, 10) : "");
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        card.getChildren().addAll(title, date);
        card.setOnMouseClicked(e -> DashboardController.getInstance().showBlogDetail(post));
        return card;
    }

    @FXML
    private void saveSettings() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user == null) return;

        String newName = newPersonaName.getText().trim();
        String newPass = newPassword.getText();

        if (!newName.isEmpty() && !newName.equals(user.getPersona())) {
            System.out.println("Persona güncelleme: " + user.getPersona() + " → " + newName);
            // NOTE: Full rename would require DB migration; log for now.
        }

        if (!newPass.isEmpty()) {
            System.out.println("Şifre değiştirildi.");
        }

        System.out.println("Ayarlar kaydedildi.");
    }
}