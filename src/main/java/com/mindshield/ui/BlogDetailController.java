package com.mindshield.ui;

import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BlogDetailController {
    @FXML
    private Button btnBack;
    @FXML
    private Button btnFavoriteBlog;
    @FXML
    private Text txtTitle;
    @FXML
    private Label lblAuthor;
    @FXML
    private Label lblDate;
    @FXML
    private Text txtContent;
    @FXML
    private VBox commentContainer;
    @FXML
    private TextField commentInput;

    @FXML
    private HBox adminControls;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnDelete;
    @FXML
    private Button btnReport;

    private BlogPost currentPost;
    private final com.mindshield.dao.PostDao postDao = new com.mindshield.dao.PostDaoImpl();

    public void setPost(BlogPost post) {
        this.currentPost = post;
        txtTitle.setText(post.getTitle());
        lblAuthor.setText(post.getAuthor().getPersona());
        lblDate.setText(post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        txtContent.setText(post.getBody());

        // Yetki kontrolü
        var user = DashboardController.getCurrentUser();
        if (user != null && user.getPersona().equals(post.getAuthor().getPersona())) {
            adminControls.setVisible(true);
        } else {
            adminControls.setVisible(false);
        }

        boolean canReport = user != null
                && post.getAuthor() != null
                && !user.getPersona().equals(post.getAuthor().getPersona())
                && user.getRole() != UserRole.ADMIN;
        if (btnReport != null) {
            btnReport.setVisible(canReport);
            btnReport.setManaged(canReport);
        }

        refreshComments();
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (btnFavoriteBlog == null || currentPost == null || DashboardController.getCurrentUser() == null) return;
        boolean isFav = postDao.isFavoriteBlog(
                DashboardController.getCurrentUser().getId(), currentPost.getId());
        btnFavoriteBlog.setText(isFav ? "Favoriden cikar" : "Favorilere ekle");
    }

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> DashboardController.getInstance().showBlog());

        btnDelete.setOnAction(e -> handleDelete());
        btnEdit.setOnAction(e -> handleEdit());

        if (btnReport != null) {
            btnReport.setOnAction(e -> handleReportPost());
        }
    }

    @FXML
    private void handleFavorite() {
        if (currentPost == null || DashboardController.getCurrentUser() == null) return;
        String userId = DashboardController.getCurrentUser().getId();
        String postId = currentPost.getId();
        if (postDao.isFavoriteBlog(userId, postId)) {
            postDao.removeFavoriteBlog(userId, postId);
        } else {
            postDao.addFavoriteBlog(userId, postId);
        }
        updateFavoriteButton();
    }

    private void handleReportPost() {
        var user = DashboardController.getCurrentUser();
        if (user == null || currentPost == null) {
            return;
        }
        TextInputDialog dlg = new TextInputDialog();
        dlg.setTitle("Blog sikayeti");
        dlg.setHeaderText(null);
        dlg.setContentText("Gerekce:");
        dlg.showAndWait().ifPresent(reason -> {
            if (reason.isBlank()) {
                return;
            }
            MainApp.moderationService.reportBlogPost(user, currentPost, reason.trim());
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setHeaderText(null);
            ok.setContentText("Sikayetiniz yoneticiye iletildi.");
            ok.showAndWait();
        });
    }

    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yazıyı Sil");
        alert.setHeaderText("Bu yazıyı silmek istediğinize emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            var user = DashboardController.getCurrentUser();
            if (user == null) {
                return;
            }
            try {
                MainApp.postService.unpublishPost(user, currentPost.getId());
                DashboardController.getInstance().showBlog();
            } catch (UnauthorizedException ex) {
                Alert err = new Alert(Alert.AlertType.WARNING);
                err.setHeaderText(null);
                err.setContentText(ex.getMessage());
                err.showAndWait();
            }
        }
    }

    private void handleEdit() {
        DashboardController.getInstance().showBlogEdit(currentPost);
    }

    private void refreshComments() {
        commentContainer.getChildren().clear();
        for (Comment comment : currentPost.getComments()) {
            VBox card = new VBox(5);
            card.setStyle("-fx-padding: 12 14; -fx-background-color: #FAFAFA; -fx-background-radius: 8; -fx-border-color: #BBAA99; -fx-border-radius: 8;");

            Label author = new Label(comment.getAuthor().getPersona());
            author.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #81A6C6;");

            Text body = new Text(comment.getBody());
            body.setWrappingWidth(500);
            body.setStyle("-fx-font-size: 13; -fx-fill: #4A5B6C;");

            card.getChildren().addAll(author, body);
            commentContainer.getChildren().add(card);
        }
    }

    @FXML
    private void addComment() {
        String body = commentInput.getText();
        if (body == null || body.isBlank() || currentPost == null) {
            return;
        }
        var user = DashboardController.getCurrentUser();
        if (user == null) {
            return;
        }
        try {
            MainApp.postService.addComment(currentPost.getId(), user, body.trim());
            commentInput.clear();
            refreshComments();
        } catch (RuntimeException ex) {
            com.mindshield.util.AppLog.severe(ex);
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setHeaderText(null);
            err.setContentText("Yorum eklenirken bir hata oluştu. Lütfen tekrar deneyin.");
            err.showAndWait();
        }
    }
}

