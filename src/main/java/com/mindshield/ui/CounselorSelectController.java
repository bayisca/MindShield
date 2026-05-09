package com.mindshield.ui;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.dao.DatabaseInitializer;
import com.mindshield.models.Counselor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CounselorSelectController {

    @FXML
    private VBox counselorContainer;

    @FXML
    public void initialize() {
        counselorContainer.getChildren().clear();

        String sql = """
            SELECT *
            FROM users
            WHERE role = 'COUNSELOR'
        """;

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {

            boolean found = false;

            while (rs.next()) {

            Counselor counselor = new Counselor(
                    rs.getString("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("profession")
            );

            // opsiyonel alanlar
            //counselor.setBio(rs.getString("bio"));

            counselorContainer.getChildren()
                    .add(buildCounselorCard(counselor));

            found = true;
        }

            if (!found) {
                Label empty = new Label("Henüz kayıtlı danışman bulunmuyor.");
                empty.setStyle("""
                    -fx-text-fill: #8A9CAE;
                    -fx-font-size: 14px;
                    -fx-padding: 20;
                """);

                counselorContainer.getChildren().add(empty);
            }

        } catch (Exception e) {
            e.printStackTrace();

            Label error = new Label("Danışmanlar yüklenemedi.");
            error.setStyle("-fx-text-fill: red;");
            counselorContainer.getChildren().add(error);
        }
    }

    private VBox buildCounselorCard(Counselor c) {

        VBox card = new VBox(10);
        card.getStyleClass().add("counselor-card");

        // Avatar row
        HBox header = new HBox(14);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle avatar = new Circle(22);
        avatar.setStyle("""
            -fx-fill: rgba(129,166,198,0.15);
            -fx-stroke: #81A6C6;
            -fx-stroke-width: 1.5;
        """);

        Label initials = new Label(
                c.getPersona()
                        .substring(0, Math.min(2, c.getPersona().length()))
                        .toUpperCase()
        );

        initials.setStyle("""
            -fx-font-weight: bold;
            -fx-font-size: 14px;
            -fx-text-fill: #81A6C6;
        """);

        javafx.scene.layout.StackPane avatarPane =
                new javafx.scene.layout.StackPane(avatar, initials);

        VBox nameBox = new VBox(3);

        Label name = new Label(c.getPersona());
        name.getStyleClass().add("counselor-name");

        Label spec = new Label("🩺 " + c.getExpertiseDisplayTitle());
        spec.getStyleClass().add("counselor-spec");

        nameBox.getChildren().addAll(name, spec);

        HBox.setHgrow(nameBox, javafx.scene.layout.Priority.ALWAYS);

        // Rating
        Label rating = new Label(
                c.getRating() > 0
                        ? "★ " + String.format("%.1f", c.getRating())
                        : "Yeni"
        );

        rating.setStyle("""
            -fx-background-color: rgba(129,166,198,0.12);
            -fx-border-color: rgba(129,166,198,0.3);
            -fx-border-radius: 20;
            -fx-background-radius: 20;
            -fx-text-fill: #81A6C6;
            -fx-font-size: 11px;
            -fx-padding: 3 10;
        """);

        header.getChildren().addAll(avatarPane, nameBox, rating);

        card.getChildren().add(header);

        // Open messages
        card.setOnMouseClicked(e -> {
            DashboardController dash = DashboardController.getInstance();

            if (dash != null) {
                dash.showMessagesWithContact(c.getPersona());
            }
        });

        return card;
    }
}