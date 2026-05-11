package com.mindshield.ui;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.mindshield.models.ForumCategory;
import com.mindshield.models.ForumReply;
import com.mindshield.models.ForumTopic;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ForumController {
    @FXML private StackPane forumStack;
    @FXML private VBox categoriesView;
    @FXML private VBox topicsView;
    @FXML private VBox createTopicView;
    @FXML private VBox topicDetailView;

    @FXML private ListView<ForumCategory> categoryList;
    @FXML private ListView<ForumTopic> topicList;
    @FXML private TextField topicSearchField;

    @FXML private Label topicPageTitle;
    @FXML private Label topicPageDescription;
    @FXML private Label createCategoryLabel;

    @FXML private TextField topicTitleField;
    @FXML private TextArea topicBodyField;

    @FXML private Label detailTitle;
    @FXML private Label detailMeta;
    @FXML private VBox repliesContainer;
    @FXML private TextArea replyBodyField;
    @FXML private javafx.scene.control.Button btnDeleteTopic;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private ForumCategory selectedCategory;
    private ForumTopic selectedTopic;

    @FXML
    public void initialize() {
        setupCategoryList();
        setupTopicList();

        categoryList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        openCategory(newV);
                    }
                });
        topicList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> {
                    if (newV != null) {
                        openTopic(newV);
                    }
                });
        if (topicSearchField != null) {
            topicSearchField.textProperty().addListener((obs, oldV, newV) -> refreshTopics(false));
        }

        refreshCategories();
        showOnly(categoriesView);
    }

    @FXML
    private void backToCategories() {
        selectedCategory = null;
        selectedTopic = null;
        topicSearchField.clear();
        refreshCategories();
        categoryList.getSelectionModel().clearSelection();
        showOnly(categoriesView);
    }

    @FXML
    private void backToTopics() {
        selectedTopic = null;
        topicList.getSelectionModel().clearSelection();
        refreshTopics(false);
        showOnly(topicsView);
    }

    @FXML
    private void showCreateTopicPage() {
        if (selectedCategory == null) {
            showWarn("Konu acmak icin once kategori secin.");
            return;
        }
        createCategoryLabel.setText(selectedCategory.getName());
        topicTitleField.clear();
        topicBodyField.clear();
        showOnly(createTopicView);
    }

    @FXML
    private void createTopic() {
        if (selectedCategory == null) {
            showWarn("Baslik acmak icin once kategori secin.");
            return;
        }
        try {
            MainApp.forumService.createTopic(
                    DashboardController.getCurrentUser(),
                    selectedCategory.getId(),
                    topicTitleField.getText(),
                    topicBodyField.getText());
            topicTitleField.clear();
            topicBodyField.clear();
            refreshCategoriesKeeping(selectedCategory.getId());
            refreshTopics(false);
            topicList.getSelectionModel().clearSelection();
            showOnly(topicsView);
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    @FXML
    private void sendReply() {
        if (selectedTopic == null) {
            showWarn("Yanit icin once bir baslik acin.");
            return;
        }
        try {
            MainApp.forumService.addReply(
                    DashboardController.getCurrentUser(),
                    selectedTopic.getId(),
                    replyBodyField.getText());
            replyBodyField.clear();
            renderTopicDetail(selectedTopic.getId());
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    @FXML
    private void deleteSelectedTopic() {
        if (selectedTopic == null) {
            showWarn("Silmek icin once bir baslik secin.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Bu baslik ve tum yanitlar silinsin mi?");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get().getButtonData().isCancelButton()) {
            return;
        }
        try {
            MainApp.forumService.deleteTopic(DashboardController.getCurrentUser(), selectedTopic.getId());
            selectedTopic = null;
            refreshCategoriesKeeping(selectedCategory != null ? selectedCategory.getId() : null);
            refreshTopics(false);
            showOnly(topicsView);
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void setupCategoryList() {
        categoryList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ForumCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(14);
                row.setStyle("-fx-padding: 18; -fx-background-color: #FCF9F5; -fx-border-color: #D2C4B4; -fx-border-radius: 12; -fx-background-radius: 12;");

                VBox copy = new VBox(6);
                HBox.setHgrow(copy, Priority.ALWAYS);
                Label name = new Label(item.getName());
                name.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
                Label desc = new Label(item.getDescription());
                desc.setWrapText(true);
                desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #6A7C8F;");
                copy.getChildren().addAll(name, desc);

                VBox stats = new VBox(2);
                stats.setStyle("-fx-alignment: CENTER_RIGHT;");
                Label count = new Label(String.valueOf(item.getTopicCount()));
                count.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #658AAB;");
                Label label = new Label("baslik");
                label.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");
                stats.getChildren().addAll(count, label);

                row.getChildren().addAll(copy, stats);
                setText(null);
                setGraphic(row);
            }
        });
    }

    private void setupTopicList() {
        topicList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ForumTopic item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                HBox row = new HBox(14);
                row.setStyle("-fx-padding: 16; -fx-background-color: #FCF9F5; -fx-border-color: #D2C4B4; -fx-border-radius: 12; -fx-background-radius: 12;");

                VBox copy = new VBox(6);
                HBox.setHgrow(copy, Priority.ALWAYS);
                Label title = new Label(item.getTitle());
                title.setWrapText(true);
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34495E;");
                String author = item.getAuthor() != null ? item.getAuthor().getPersona() : "Bilinmeyen";
                Label meta = new Label(author + " - " + DATE_FMT.format(item.getCreatedAt()));
                meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");
                copy.getChildren().addAll(title, meta);

                VBox stats = new VBox(2);
                stats.setStyle("-fx-alignment: CENTER_RIGHT;");
                Label count = new Label(String.valueOf(item.getReplyCount()));
                count.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #658AAB;");
                Label label = new Label("yanit");
                label.setStyle("-fx-font-size: 11px; -fx-text-fill: #8A9CAE;");
                stats.getChildren().addAll(count, label);

                row.getChildren().addAll(copy, stats);
                setText(null);
                setGraphic(row);
            }
        });
    }

    private void refreshCategories() {
        categoryList.setItems(FXCollections.observableArrayList(MainApp.forumService.getCategories()));
    }

    private void refreshCategoriesKeeping(String categoryId) {
        refreshCategories();
        if (categoryId == null) {
            return;
        }
        categoryList.getItems().stream()
                .filter(c -> categoryId.equals(c.getId()))
                .findFirst()
                .ifPresent(c -> selectedCategory = c);
    }

    private void openCategory(ForumCategory category) {
        selectedCategory = category;
        selectedTopic = null;
        topicPageTitle.setText(category.getName());
        topicPageDescription.setText(category.getDescription());
        refreshTopics(false);
        showOnly(topicsView);
    }

    private void refreshTopics(boolean keepSelection) {
        if (selectedCategory == null) {
            topicList.setItems(FXCollections.observableArrayList());
            return;
        }
        String selectedId = keepSelection && selectedTopic != null ? selectedTopic.getId() : null;
        String query = topicSearchField != null ? topicSearchField.getText() : "";
        topicList.setItems(FXCollections.observableArrayList(
                MainApp.forumService.getTopicsByCategory(selectedCategory.getId(), query)));
        if (selectedId != null) {
            topicList.getItems().stream()
                    .filter(t -> selectedId.equals(t.getId()))
                    .findFirst()
                    .ifPresent(topicList.getSelectionModel()::select);
        }
    }

    private void openTopic(ForumTopic topicRef) {
        if (topicRef == null) {
            return;
        }
        renderTopicDetail(topicRef.getId());
        showOnly(topicDetailView);
    }

    private void renderTopicDetail(String topicId) {
        ForumTopic topic = MainApp.forumService.findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        selectedTopic = topic;
        repliesContainer.getChildren().clear();

        detailTitle.setText(topic.getTitle());
        String author = topic.getAuthor() != null ? topic.getAuthor().getPersona() : "Bilinmeyen";
        detailMeta.setText(topic.getCategoryName() + " - " + author + " - " + DATE_FMT.format(topic.getCreatedAt()));
        setDeleteVisible(canManageTopic(topic));

        VBox bodyCard = new VBox(8);
        bodyCard.setStyle("-fx-padding: 18; -fx-background-color: #FCF9F5; -fx-border-color: #D2C4B4; -fx-border-radius: 12; -fx-background-radius: 12;");
        Label bodyTitle = new Label("Konu");
        bodyTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #658AAB;");
        Label body = new Label(topic.getBody());
        body.setWrapText(true);
        body.setStyle("-fx-font-size: 14px; -fx-text-fill: #4A5B6C;");
        bodyCard.getChildren().addAll(bodyTitle, body);
        repliesContainer.getChildren().add(bodyCard);

        for (ForumReply reply : topic.getReplies()) {
            repliesContainer.getChildren().add(createReplyCard(topic, reply));
        }
    }

    private VBox createReplyCard(ForumTopic topic, ForumReply reply) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 14; -fx-background-color: #FAFAFA; -fx-border-color: #D2C4B4; -fx-border-radius: 12; -fx-background-radius: 12;");
        HBox header = new HBox(8);
        String author = reply.getAuthor() != null ? reply.getAuthor().getPersona() : "Bilinmeyen";
        Label meta = new Label(author + " - " + DATE_FMT.format(reply.getCreatedAt()));
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
        text.setStyle("-fx-text-fill: #4A5B6C;");
        card.getChildren().addAll(header, text);
        return card;
    }

    private void editReply(ForumTopic topic, ForumReply reply) {
        TextInputDialog dialog = new TextInputDialog(reply.getBody());
        dialog.setHeaderText(null);
        dialog.setContentText("Yeni yazi:");
        dialog.setTitle("Yaniti duzenle");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        try {
            MainApp.forumService.editReply(
                    DashboardController.getCurrentUser(),
                    topic.getId(),
                    reply.getId(),
                    result.get());
            renderTopicDetail(topic.getId());
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
        }
    }

    private void deleteReply(ForumTopic topic, ForumReply reply) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Bu yanit silinsin mi?");
        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isEmpty() || choice.get().getButtonData().isCancelButton()) {
            return;
        }
        try {
            MainApp.forumService.deleteReply(
                    DashboardController.getCurrentUser(),
                    topic.getId(),
                    reply.getId());
            renderTopicDetail(topic.getId());
        } catch (IllegalArgumentException ex) {
            showWarn(ex.getMessage());
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

    private void setDeleteVisible(boolean visible) {
        if (btnDeleteTopic != null) {
            btnDeleteTopic.setVisible(visible);
            btnDeleteTopic.setManaged(visible);
        }
    }

    private void showOnly(VBox view) {
        for (var node : forumStack.getChildren()) {
            boolean active = node == view;
            node.setVisible(active);
            node.setManaged(active);
        }
    }

    private void showWarn(String message) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
