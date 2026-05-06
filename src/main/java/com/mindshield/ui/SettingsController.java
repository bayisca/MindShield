package com.mindshield.ui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Counselor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private TextField     newPersonaName;
    @FXML private PasswordField newPassword;
    @FXML private VBox          myPostsContainer;
    @FXML private VBox          myPostsSection;

    @FXML private VBox          counselorSummarySection;
    @FXML private Label         lblJoinedDate;
    @FXML private Label         lblExpertiseTitle;
    @FXML private Label         lblBlogPostCount;

    @FXML
    public void initialize() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user == null) {
            return;
        }

        newPersonaName.setText(user.getPersona());

        boolean counselor = user.getRole() == UserRole.COUNSELOR;
        if (counselorSummarySection != null) {
            counselorSummarySection.setVisible(counselor);
            counselorSummarySection.setManaged(counselor);
        }
        if (counselor && user instanceof Counselor c) {
            if (lblJoinedDate != null) {
                lblJoinedDate.setText("Kayıt tarihi: " + user.getRegisteredAt());
            }
            if (lblExpertiseTitle != null) {
                lblExpertiseTitle.setText("Ünvan: " + c.getExpertiseDisplayTitle());
            }
            if (lblBlogPostCount != null) {
                long n = MainApp.postService.getAllPosts().stream()
                        .filter(p -> p != null && p.getAuthor() != null
                                && user.getPersona().equals(p.getAuthor().getPersona()))
                        .count();
                lblBlogPostCount.setText("Blog yazısı sayısı: " + n);
            }
        }

        boolean canHavePosts =
                user.getRole() == UserRole.COUNSELOR || user.getRole() == UserRole.ADMIN;

        if (myPostsSection != null) {
            myPostsSection.setVisible(canHavePosts);
            myPostsSection.setManaged(canHavePosts);
        }

        if (myPostsContainer != null) {
            myPostsContainer.setVisible(canHavePosts);
            myPostsContainer.setManaged(canHavePosts);
        }

        if (canHavePosts) {
            loadMyPosts(user);
        }
    }

    private void loadMyPosts(BaseUser user) {
        if (myPostsContainer == null) {
            return;
        }
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
        if (user == null) {
            return;
        }

        String newPass = newPassword.getText();
        if (!newPass.isEmpty()) {
            String passErr = com.mindshield.util.PasswordRules.validate(newPass);
            if (passErr != null) {
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setHeaderText(null);
                a.setContentText(passErr);
                a.showAndWait();
                return;
            }
            user.setPassword(newPass);
            newPassword.clear();
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(null);
            ok.setContentText("Şifre güncellendi.");
            ok.showAndWait();
        }
    }

    @FXML
    private void deleteAccount() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user == null) {
            return;
        }
        if (user.getRole() == UserRole.ADMIN) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText(null);
            a.setContentText("Yönetici hesabı bu ekrandan silinemez.");
            a.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Hesabınız kalıcı olarak silinecek. Emin misiniz?");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get() != ButtonType.OK) {
            return;
        }

        MainApp.deleteAccountAndCleanup(user);
        DashboardController.setCurrentUser(null);

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) newPersonaName.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield — Giriş");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
