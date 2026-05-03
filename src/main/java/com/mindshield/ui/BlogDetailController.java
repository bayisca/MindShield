package com.mindshield.ui;

import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class BlogDetailController {
    @FXML
    private Button btnBack;
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

    private BlogPost currentPost;

    public void setPost(BlogPost post) {
        this.currentPost = post;
        txtTitle.setText(post.getTitle());
        lblAuthor.setText("✍ " + post.getAuthor().getPersona());
        lblDate.setText(post.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        txtContent.setText(post.getBody());

        // Yetki kontrolü
        var user = DashboardController.getCurrentUser();
        if (user != null && user.getPersona().equals(post.getAuthor().getPersona())) {
            adminControls.setVisible(true);
        } else {
            adminControls.setVisible(false);
        }

        refreshComments();
    }

    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> DashboardController.getInstance().showBlog());

        btnDelete.setOnAction(e -> handleDelete());
        btnEdit.setOnAction(e -> handleEdit());
    }

    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Yazıyı Sil");
        alert.setHeaderText("Bu yazıyı silmek istediğinize emin misiniz?");
        alert.setContentText("Bu işlem geri alınamaz.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            var user = DashboardController.getCurrentUser();
            if (user == null) user = MainApp.userDatabase.get("admin");
            MainApp.postService.unpublishPost(user, currentPost.getId());
            DashboardController.getInstance().showBlog();
        }
    }

    private void handleEdit() {
        DashboardController.getInstance().showBlogEdit(currentPost);
    }

    private void refreshComments() {
        commentContainer.getChildren().clear();
        for (Comment comment : currentPost.getComments()) {
            VBox card = new VBox(5);
            card.getStyleClass().add("card");
            card.setStyle("-fx-padding: 12; -fx-background-color: #f8f9fa;");

            Label author = new Label(comment.getAuthor().getPersona());
            author.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: -fx-primary;");

            Text body = new Text(comment.getBody());
            body.setWrappingWidth(500);
            body.setStyle("-fx-font-size: 14;");

            card.getChildren().addAll(author, body);
            commentContainer.getChildren().add(card);
        }
    }

    @FXML
    private void addComment() {
        String body = commentInput.getText();
        if (body != null && !body.isEmpty()) {
            var user = DashboardController.getCurrentUser();
            if (user == null)
                user = MainApp.userDatabase.get("admin");

            MainApp.postService.addComment(currentPost.getId(), user, body);

            commentInput.clear();
            refreshComments();
        }
    }
}
