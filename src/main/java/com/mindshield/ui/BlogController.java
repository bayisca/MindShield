package com.mindshield.ui;

import com.mindshield.models.BlogPost;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class BlogController {

    @FXML private Button btnWrite;
    @FXML private VBox   blogContainer;

    @FXML
    public void initialize() {
        refreshBlogPosts();
        btnWrite.setOnAction(e -> handleNewPost());
    }

    public void refreshBlogPosts() {
        if (blogContainer == null) return;
        blogContainer.getChildren().clear();

        for (BlogPost post : MainApp.postService.getAllPosts()) {
            blogContainer.getChildren().add(createPostCard(post));
        }

        if (blogContainer.getChildren().isEmpty()) {
            Label empty = new Label("Henuz hic uzman yazisi yok.");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-padding: 30;");
            blogContainer.getChildren().add(empty);
        }
    }

    private VBox createPostCard(BlogPost post) {
        VBox card = new VBox(14);
        card.setStyle(
            "-fx-background-color: #162032;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #1e3048;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 14, 0, 0, 4);" +
            "-fx-padding: 22; -fx-cursor: hand;"
        );

        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0; -fx-wrap-text: true;");
        title.setWrapText(true);

        String preview = post.getBody().length() > 120
                ? post.getBody().substring(0, 120).replace("\n", " ") + "..."
                : post.getBody().replace("\n", " ");
        Label body = new Label(preview);
        body.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        body.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label authorBadge = new Label(post.getAuthor().getPersona());
        authorBadge.setStyle(
            "-fx-background-color: rgba(20,184,166,0.12);" +
            "-fx-border-color: rgba(20,184,166,0.3);" +
            "-fx-border-radius: 20; -fx-background-radius: 20;" +
            "-fx-text-fill: #14b8a6; -fx-font-size: 11px;" +
            "-fx-font-weight: bold; -fx-padding: 3 12;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dateLbl = new Label(post.getCreatedAt() != null
                ? post.getCreatedAt().toString().substring(0, 10) : "");
        dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");

        Label readMore = new Label("Devamini oku >");
        readMore.setStyle("-fx-font-size: 11px; -fx-text-fill: #14b8a6; -fx-cursor: hand;");

        footer.getChildren().addAll(authorBadge, spacer, dateLbl, readMore);
        card.getChildren().addAll(title, body, footer);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #1a2a3d;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(20,184,166,0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(20,184,166,0.15), 20, 0, 0, 4);" +
            "-fx-padding: 22; -fx-cursor: hand;"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: #162032;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #1e3048;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 14, 0, 0, 4);" +
            "-fx-padding: 22; -fx-cursor: hand;"
        ));

        card.setOnMouseClicked(e -> DashboardController.getInstance().showBlogDetail(post));
        return card;
    }

    private void handleNewPost() {
        DashboardController.getInstance().showBlogWrite();
    }

    public void hideWriteButton() {
        if (btnWrite != null) {
            btnWrite.setVisible(false);
            btnWrite.setManaged(false);
        }
    }
}