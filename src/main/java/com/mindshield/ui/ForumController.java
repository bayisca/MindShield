package com.mindshield.ui;

import java.time.format.DateTimeFormatter;

import com.mindshield.models.ForumReply;
import com.mindshield.models.ForumTopic;
import com.mindshield.ui.UserRole;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.util.Optional;

public class ForumController {
    @FXML private ListView<ForumTopic> topicList;
    @FXML private VBox repliesContainer;
    @FXML private TextField searchField;
    @FXML private TextField topicTitleField;
    @FXML private TextArea topicBodyField;
    @FXML private TextArea replyBodyField;
    @FXML private Label selectedTopicTitle;
    @FXML private Label selectedTopicMeta;
    @FXML private javafx.scene.control.Button btnDeleteTopic;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    public void initialize() {
        topicList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ForumTopic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTitle() + " (" + item.getReplies().size() + " yanit)");
                }
            }
        });

        topicList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> renderSelectedTopic(newV));
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> refreshTopics());
        }

        refreshTopics();
    }

    @FXML
    private void createTopic() {
        try {
            MainApp.forumService.createTopic(
                    DashboardController.getCurrentUser(),
                    topicTitleField.getText(),
                    topicBodyField.getText()
            );
            topicTitleField.clear();
            topicBodyField.clear();
            refreshTopics();
            topicList.getSelectionModel().selectFirst();
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    @FXML
    private void sendReply() {
        ForumTopic selected = topicList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Yanit icin once bir baslik secin.");
            return;
        }
        try {
            MainApp.forumService.addReply(
                    DashboardController.getCurrentUser(),
                    selected.getId(),
                    replyBodyField.getText()
            );
            replyBodyField.clear();
            renderSelectedTopic(selected);
            refreshTopics();
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void refreshTopics() {
        String query = searchField != null ? searchField.getText() : "";
        topicList.setItems(FXCollections.observableArrayList(MainApp.forumService.searchTopics(query)));
        if (!topicList.getItems().isEmpty() && topicList.getSelectionModel().getSelectedItem() == null) {
            topicList.getSelectionModel().selectFirst();
        }
    }

    private void renderSelectedTopic(ForumTopic topic) {
        repliesContainer.getChildren().clear();
        if (topic == null) {
            selectedTopicTitle.setText("Baslik seciniz");
            selectedTopicMeta.setText("");
            if (btnDeleteTopic != null) {
                btnDeleteTopic.setVisible(false);
                btnDeleteTopic.setManaged(false);
            }
            return;
        }

        selectedTopicTitle.setText(topic.getTitle());
        selectedTopicMeta.setText("Acan: " + topic.getAuthor().getPersona() + " • " + DATE_FMT.format(topic.getCreatedAt()));
        if (btnDeleteTopic != null) {
            boolean canDelete = canManageTopic(topic);
            btnDeleteTopic.setVisible(canDelete);
            btnDeleteTopic.setManaged(canDelete);
        }

        Label body = new Label(topic.getBody());
        body.setWrapText(true);
        body.setStyle("-fx-padding: 10; -fx-background-color: #FCF9F5; -fx-border-color: #D2C4B4; -fx-border-radius: 8; -fx-background-radius: 8;");
        repliesContainer.getChildren().add(body);

        for (ForumReply reply : topic.getReplies()) {
            VBox card = new VBox(4);
            card.setStyle("-fx-padding: 10; -fx-background-color: #FAFAFA; -fx-border-color: #D2C4B4; -fx-border-radius: 8; -fx-background-radius: 8;");
            HBox header = new HBox(8);
            Label meta = new Label(reply.getAuthor().getPersona() + " • " + DATE_FMT.format(reply.getCreatedAt()));
            meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(meta, spacer);

            if (canManageReply(reply)) {
                Hyperlink editLink = new Hyperlink("Duzenle");
                editLink.setOnAction(e -> editReply(topic, reply));
                Hyperlink deleteLink = new Hyperlink("Sil");
                deleteLink.setOnAction(e -> deleteReply(topic, reply));
                header.getChildren().addAll(editLink, deleteLink);
            }

            Label text = new Label(reply.getBody());
            text.setWrapText(true);
            card.getChildren().addAll(header, text);
            repliesContainer.getChildren().add(card);
        }
    }

    private boolean canManageReply(ForumReply reply) {
        var current = DashboardController.getCurrentUser();
        return current != null && (reply.isAuthor(current) || current.getRole() == UserRole.ADMIN);
    }

    private boolean canManageTopic(ForumTopic topic) {
        var current = DashboardController.getCurrentUser();
        return current != null && (topic.isAuthor(current) || current.getRole() == UserRole.ADMIN);
    }

    @FXML
    private void deleteSelectedTopic() {
        ForumTopic selected = topicList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Silmek icin once bir baslik secin.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Bu baslik ve tum yanitlar silinsin mi?");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ButtonType.OK) return;
        try {
            MainApp.forumService.deleteTopic(DashboardController.getCurrentUser(), selected.getId());
            refreshTopics();
            ForumTopic now = topicList.getSelectionModel().getSelectedItem();
            renderSelectedTopic(now);
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void editReply(ForumTopic topic, ForumReply reply) {
        TextInputDialog dialog = new TextInputDialog(reply.getBody());
        dialog.setHeaderText(null);
        dialog.setContentText("Yeni yazi:");
        dialog.setTitle("Yaniti duzenle");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;
        try {
            MainApp.forumService.editReply(
                    DashboardController.getCurrentUser(),
                    topic.getId(),
                    reply.getId(),
                    result.get()
            );
            renderSelectedTopic(topic);
            refreshTopics();
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void deleteReply(ForumTopic topic, ForumReply reply) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Bu yanit silinsin mi?");
        ButtonType ok = ButtonType.OK;
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get() != ok) return;
        try {
            MainApp.forumService.deleteReply(
                    DashboardController.getCurrentUser(),
                    topic.getId(),
                    reply.getId()
            );
            renderSelectedTopic(topic);
            refreshTopics();
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void showWarn(String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
