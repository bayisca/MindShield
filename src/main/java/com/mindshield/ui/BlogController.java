package com.mindshield.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import java.util.Optional;
import javafx.scene.control.TextInputDialog;

public class BlogController {
    @FXML private Button btnWrite;
    @FXML private VBox blogContainer;

    @FXML
    public void initialize() {
        refreshBlogPosts();
        
        btnWrite.getStyleClass().add("button");
        btnWrite.setOnAction(e -> handleNewPost());
    }

    public void refreshBlogPosts() {
        if (blogContainer == null) return;
        
        blogContainer.getChildren().clear();
        for (com.mindshield.models.BlogPost post : MainApp.blogPosts) {
            blogContainer.getChildren().add(createPostCard(post));
        }
    }

    private VBox createPostCard(com.mindshield.models.BlogPost post) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "card-clickable");
        card.setStyle("-fx-padding: 20;"); // Keep padding here as it's card-specific content spacing

        Text title = new Text(post.getTitle());
        title.getStyleClass().add("title-text");
        title.setStyle("-fx-font-size: 18;");

        Text body = new Text(post.getBody());
        body.setWrappingWidth(550);
        body.getStyleClass().add("subtitle-text");

        HBox footer = new HBox();
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label author = new Label("✍ " + post.getAuthor().getPersona());
        author.setStyle("-fx-font-weight: bold; -fx-text-fill: #3f51b5;");
        footer.getChildren().add(author);

        card.getChildren().addAll(title, body, footer);

        card.setOnMouseClicked(e -> {
            DashboardController.getInstance().showBlogDetail(post);
        });

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