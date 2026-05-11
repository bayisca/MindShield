package com.mindshield.ui;

import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BlogPost;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class BlogWriteController {
    @FXML private Button btnCancel;
    @FXML private Button btnPublish;
    @FXML private TextField fldTitle;
    @FXML private TextArea fldContent;
    @FXML private Label lblAuthor;

    private BlogPost editingPost;

    @FXML
    public void initialize() {
        var user = DashboardController.getCurrentUser();
        if (user != null) {
            lblAuthor.setText(user.getPersona());
        }

        btnCancel.setOnAction(e -> {
            if (editingPost != null) {
                DashboardController.getInstance().showBlogDetail(editingPost);
            } else {
                DashboardController.getInstance().showBlog();
            }
        });

        btnPublish.setOnAction(e -> publishContent());
    }

    public void setEditPost(BlogPost post) {
        this.editingPost = post;
        fldTitle.setText(post.getTitle());
        fldContent.setText(post.getBody());
        btnPublish.setText("Değişiklikleri Kaydet");
    }

    private void publishContent() {
        String title = fldTitle.getText();
        String body = fldContent.getText();
        if (title == null || title.isBlank() || body == null || body.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Eksik bilgi", "Başlık ve içerik boş olamaz.");
            return;
        }

        var user = DashboardController.getCurrentUser();
        if (user == null) {
            showAlert(Alert.AlertType.WARNING, "Oturum", "Yayınlamak için giriş yapmalısınız.");
            return;
        }
        if (!MainApp.postService.canPublishBlogPosts(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Blog paylaşımı",
                    "Blog yazısı yalnızca danışman hesaplarıyla oluşturulabilir veya düzenlenebilir.");
            return;
        }

        try {
            if (editingPost != null) {
                MainApp.postService.updatePost(user, editingPost.getId(), title, body);
                BlogPost refreshed = MainApp.postService.findPostById(editingPost.getId());
                if (refreshed != null) {
                    DashboardController.getInstance().showBlogDetail(refreshed);
                } else {
                    DashboardController.getInstance().showBlog();
                }
            } else {
                MainApp.postService.createPost(user, title, body);
                DashboardController.getInstance().showBlog();
            }
        } catch (UnauthorizedException ex) {
            showAlert(Alert.AlertType.WARNING, "İşlem yapılamadı", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showAlert(Alert.AlertType.WARNING, "Geçersiz içerik", ex.getMessage());
        } catch (RuntimeException ex) {
            com.mindshield.util.AppLog.severe(ex);
            showAlert(Alert.AlertType.ERROR, "Kayıt hatası",
                    "Yazı kaydedilirken bir hata oluştu. Lütfen tekrar deneyin.");
        }
    }

    private static void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
