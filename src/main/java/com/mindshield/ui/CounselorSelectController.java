package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Displayed to CLIENT / ANONYMOUS users so they can browse and select a
 * Danışman (counselor) to start a conversation with.
 */
public class CounselorSelectController {

    @FXML private VBox counselorContainer;

    @FXML
    public void initialize() {
        counselorContainer.getChildren().clear();

        for (BaseUser user : MainApp.userDatabase.values()) {
            if (user.getRole() == UserRole.COUNSELOR) {
                counselorContainer.getChildren().add(buildCounselorCard((Counselor) user));
            }
        }

        if (counselorContainer.getChildren().isEmpty()) {
            Label empty = new Label("Henüz kayıtlı danışman bulunmuyor.");
            empty.setStyle("-fx-text-fill: #475569; -fx-font-size: 14px; -fx-padding: 20;");
            counselorContainer.getChildren().add(empty);
        }
    }

    private VBox buildCounselorCard(Counselor c) {
        VBox card = new VBox(10);
        card.getStyleClass().add("counselor-card");

        // Avatar circle + name row
        HBox header = new HBox(14);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle avatar = new Circle(22);
        avatar.setStyle("-fx-fill: rgba(20,184,166,0.15); -fx-stroke: #14b8a6; -fx-stroke-width: 1.5;");

        Label initials = new Label(c.getPersona().substring(0, Math.min(2, c.getPersona().length())).toUpperCase());
        initials.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #14b8a6;");

        javafx.scene.layout.StackPane avatarPane = new javafx.scene.layout.StackPane(avatar, initials);

        VBox nameBox = new VBox(3);
        Label name = new Label(c.getPersona());
        name.getStyleClass().add("counselor-name");

        Label spec = new Label("🩺 " + c.getExpertiseDisplayTitle());
        spec.getStyleClass().add("counselor-spec");

        nameBox.getChildren().addAll(name, spec);
        HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);

        // Rating badge
        Label rating = new Label(c.getRating() > 0 ? "★ " + String.format("%.1f", c.getRating()) : "Yeni");
        rating.setStyle("-fx-background-color: rgba(20,184,166,0.12); -fx-border-color: rgba(20,184,166,0.3);" +
                        "-fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #14b8a6;" +
                        "-fx-font-size: 11px; -fx-padding: 3 10;");

        header.getChildren().addAll(avatarPane, nameBox, rating);

        // Approval notice
        if (!c.isApproved()) {
            Label pending = new Label("⏳ Onay bekliyor — mesajlar iletilecek");
            pending.setStyle("-fx-font-size: 11px; -fx-text-fill: #fbbf24;");
            card.getChildren().addAll(header, pending);
        } else {
            card.getChildren().add(header);
        }

        // On click → open Messages with this counselor pre-selected
        card.setOnMouseClicked(e -> {
            DashboardController dash = DashboardController.getInstance();
            if (dash != null) {
                dash.showMessagesWithContact(c.getPersona());
            }
        });

        return card;
    }
}
