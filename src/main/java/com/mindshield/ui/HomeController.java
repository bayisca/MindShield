package com.mindshield.ui;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Counselor;
import com.mindshield.models.ForumTopic;

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

public class HomeController {

    @FXML private Label lblGreeting;
    @FXML private Label lblQuote;

    // Profile & Settings Fields
    @FXML private TextField     newPersonaName;
    @FXML private PasswordField newPassword;
    @FXML private VBox          myPostsContainer;
    @FXML private VBox          myPostsSection;
    @FXML private VBox          counselorSummarySection;
    @FXML private Label         lblJoinedDate;
    @FXML private Label         lblExpertiseTitle;
    @FXML private Label         lblBlogPostCount;
    @FXML private VBox          favSongsContainer;
    @FXML private VBox          favBlogsContainer;
    @FXML private VBox          myForumTopicsContainer;

    private final String[] quotes = {
        "Her yeni gün, taze bir başlangıçtır.",
        "Kendine iyi bakmak, bencillik değil gerekliliktir.",
        "Duygularını hissetmek için kendine izin ver.",
        "Bazen en büyük cesaret, yardım istemektir.",
        "Zihnin senin bahçendir; umut ek, huzur biç."
    };

    @FXML
    public void initialize() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user != null) {
            lblGreeting.setText("Hos Geldin, " + user.getPersona());
            
            // Populate Settings/Profile
            if (newPersonaName != null) {
                newPersonaName.setText(user.getPersona());
            }

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

            boolean canHavePosts = user.getRole() == UserRole.COUNSELOR || user.getRole() == UserRole.ADMIN;

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

            loadFavorites(user);
            loadMyForumTopics(user);

        } else {
            lblGreeting.setText("Hos Geldin");
        }

        // Pick a random quote
        int idx = (int)(Math.random() * quotes.length);
        if (lblQuote != null) {
            lblQuote.setText("Günün Sözü: \"" + quotes[idx] + "\"");
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
        card.setStyle("-fx-padding: 12; -fx-background-color: #FAFAF5;" +
                      "-fx-background-radius: 8; -fx-border-color: #D2C4B4; -fx-border-radius: 8;" +
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

    private void loadFavorites(BaseUser user) {
        if (favSongsContainer != null) {
            favSongsContainer.getChildren().clear();
            List<String> songs = user.getFavoriteSongTitles();
            if (songs.isEmpty()) {
                Label empty = new Label("Henüz favori şarkınız yok.");
                empty.setStyle("-fx-text-fill: #8A9CAE; -fx-font-size: 13px;");
                favSongsContainer.getChildren().add(empty);
            } else {
                for (String song : songs) {
                    Label lbl = new Label("Sarki: " + song);
                    lbl.setStyle("-fx-padding: 8; -fx-background-color: #FAFAF5; -fx-background-radius: 6; -fx-border-color: #D2C4B4; -fx-border-radius: 6; -fx-text-fill: #34495E;");
                    lbl.setMaxWidth(Double.MAX_VALUE);
                    favSongsContainer.getChildren().add(lbl);
                }
            }
        }

        if (favBlogsContainer != null) {
            favBlogsContainer.getChildren().clear();
            List<String> blogIds = user.getFavoriteBlogIds();
            List<BlogPost> favPosts = MainApp.postService.getAllPosts().stream()
                    .filter(p -> blogIds.contains(p.getId().toString()))
                    .collect(Collectors.toList());

            if (favPosts.isEmpty()) {
                Label empty = new Label("Henüz favori blogunuz yok.");
                empty.setStyle("-fx-text-fill: #8A9CAE; -fx-font-size: 13px;");
                favBlogsContainer.getChildren().add(empty);
            } else {
                for (BlogPost post : favPosts) {
                    favBlogsContainer.getChildren().add(createMiniCard(post));
                }
            }
        }
    }

    private void loadMyForumTopics(BaseUser user) {
        if (myForumTopicsContainer == null) {
            return;
        }
        myForumTopicsContainer.getChildren().clear();

        List<ForumTopic> myTopics = MainApp.forumService.getTopicsByAuthor(user);
        if (myTopics.isEmpty()) {
            Label empty = new Label("Henuz acilmis forum basliginiz yok.");
            empty.setStyle("-fx-text-fill: #8A9CAE; -fx-font-size: 13px;");
            myForumTopicsContainer.getChildren().add(empty);
            return;
        }

        for (ForumTopic topic : myTopics) {
            VBox card = new VBox(4);
            card.setStyle("-fx-padding: 10; -fx-background-color: #FAFAF5; -fx-background-radius: 8; -fx-border-color: #D2C4B4; -fx-border-radius: 8; -fx-cursor: hand;");
            Label title = new Label(topic.getTitle());
            title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
            Label meta = new Label(topic.getReplies().size() + " yanit");
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");
            card.getChildren().addAll(title, meta);
            card.setOnMouseClicked(e -> DashboardController.getInstance().showForum());
            myForumTopicsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void saveSettings() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user == null) {
            return;
        }

        boolean updated = false;
        if (newPersonaName != null) {
            String persona = newPersonaName.getText() == null ? "" : newPersonaName.getText().trim();
            if (!persona.isEmpty() && !persona.equals(user.getPersona())) {
                user.setPersona(persona);
                if (lblGreeting != null) {
                    lblGreeting.setText("Hos Geldin, " + user.getPersona());
                }
                DashboardController dashboard = DashboardController.getInstance();
                if (dashboard != null) {
                    dashboard.refreshUserBadge();
                }
                updated = true;
            }
        }

        if (newPassword != null) {
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
                updated = true;
            }
        }

        if (updated) {
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(null);
            ok.setContentText("Profil ve ayarlar güncellendi.");
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
            Stage stage = null;
            if (newPersonaName != null && newPersonaName.getScene() != null) {
                stage = (Stage) newPersonaName.getScene().getWindow();
            } else if (lblGreeting != null && lblGreeting.getScene() != null) {
                stage = (Stage) lblGreeting.getScene().getWindow();
            }
            if (stage != null) {
                stage.getScene().setRoot(root);
                stage.setTitle("MindShield — Giriş");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void navToBlog() {
        DashboardController.getInstance().showBlog();
    }

    @FXML
    private void navToForum() {
        DashboardController.getInstance().showForum();
    }

    @FXML
    private void navToMessages() {
        DashboardController.getInstance().showMessages();
    }

    @FXML
    private void navToMeditation() {
        DashboardController.getInstance().showMeditation();
    }

    @FXML
    private void navToJourShield() {
        DashboardController.getInstance().showJourShield();
    }
}
