package com.mindshield.ui;

import java.io.IOException;
import java.time.LocalDate;
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
import javafx.stage.Window;

import com.mindshield.services.MeditationPlaybackService;

public class SettingsController {

    @FXML private TextField     newPersonaName;
    @FXML private PasswordField newPassword;
    @FXML private VBox          myPostsContainer;
    @FXML private VBox          myPostsSection;

    @FXML private VBox          counselorSummarySection;
    @FXML private Label         lblJoinedDate;
    @FXML private Label         lblExpertiseTitle;
    @FXML private Label         lblBlogPostCount;
    @FXML private Label         lblForumCount;
    @FXML private Label         lblHelpedClientsCount;

    @FXML private VBox          clientSummarySection;
    @FXML private Label         lblClientJoinedDate;
    @FXML private Label         lblClientForumCount;

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
        boolean clientProfile =
                user.getRole() == UserRole.CLIENT;
        if (clientSummarySection != null) {
            clientSummarySection.setVisible(clientProfile);
            clientSummarySection.setManaged(clientProfile);
        }
        if (clientProfile) {
            LocalDate regDb = MainApp.getUserRegistrationDateFromDb(user.getId());
            if (lblClientJoinedDate != null) {
                LocalDate shown = regDb != null ? regDb : user.getRegisteredAt();
                lblClientJoinedDate.setText("Kayıt tarihi: " + shown);
            }
            if (lblClientForumCount != null) {
                long n = MainApp.forumService.getTopicsByAuthor(user).size();
                lblClientForumCount.setText("Açtığı forum sayısı: " + n);
            }
        }

        if (counselor && user instanceof Counselor c) {
            LocalDate regDb = MainApp.getUserRegistrationDateFromDb(user.getId());
            if (lblJoinedDate != null) {
                LocalDate shown = regDb != null ? regDb : user.getRegisteredAt();
                lblJoinedDate.setText("Kayıt tarihi: " + shown);
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
            if (lblForumCount != null) {
                long n = MainApp.forumService.getTopicsByAuthor(user).size();
                lblForumCount.setText("Açtığı forum sayısı: " + n);
            }
            if (lblHelpedClientsCount != null) {
                long n = MainApp.messageService.getHelpedClientsCount(user);
                lblHelpedClientsCount.setText("Yardım ettiği danışan sayısı: " + n);
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
            empty.setStyle("-fx-text-fill: #8A9CAE; -fx-font-size: 13px;");
            myPostsContainer.getChildren().add(empty);
        } else {
            for (BlogPost post : myPosts) {
                myPostsContainer.getChildren().add(createMiniCard(post));
            }
        }
    }

    private VBox createMiniCard(BlogPost post) {
        VBox card = new VBox(6);
        card.setStyle("-fx-padding: 12; -fx-background-color: #D2C4B4;" +
                      "-fx-background-radius: 8; -fx-border-color: #BBAA99; -fx-border-radius: 8;" +
                      "-fx-cursor: hand;");

        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #34495E;");

        Label date = new Label(post.getCreatedAt() != null
                ? post.getCreatedAt().toString().substring(0, 10) : "");
        date.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");

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
            // DB'ye kaydet
            try (java.sql.Connection conn = com.mindshield.dao.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET password = ? WHERE id = ?")) {
                ps.setString(1, newPass);
                ps.setString(2, user.getId());
                ps.executeUpdate();
            } catch (Exception e) {
                com.mindshield.util.AppLog.severe(e);
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setHeaderText(null);
                err.setContentText("Şifre güncellenirken hata oluştu.");
                err.showAndWait();
                return;
            }
            // In-memory nesneyi de güncelle
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
        Window owner = newPersonaName != null && newPersonaName.getScene() != null
                ? newPersonaName.getScene().getWindow()
                : null;

        if (user.getRole() == UserRole.ADMIN) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.initOwner(owner);
            a.setHeaderText(null);
            a.setContentText("Yönetici hesabı bu ekrandan silinemez.");
            a.showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(owner);
        confirm.setHeaderText(null);
        confirm.setContentText("Hesabınız kalıcı olarak silinecek. Emin misiniz?");
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isEmpty() || r.get().getButtonData().isCancelButton()) {
            return;
        }

        MeditationPlaybackService.getInstance().stopPlayback();
        MainApp.deleteAccountAndCleanup(user);
        DashboardController.setCurrentUser(null);

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Login.fxml"));
            Stage stage = (Stage) newPersonaName.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("MindShield — Giriş");
        } catch (IOException e) {
            com.mindshield.util.AppLog.severe(e);
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.initOwner(owner);
            err.setHeaderText(null);
            err.setContentText("Giriş ekranına dönülürken hata oluştu. Uygulamayı yeniden başlatın.");
            err.showAndWait();
        }
    }
}
