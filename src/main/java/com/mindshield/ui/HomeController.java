package com.mindshield.ui;

import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.ForumTopic;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML private Label lblGreeting;
    @FXML private Label lblQuote;

    @FXML private VBox          favSongsContainer;
    @FXML private VBox          favBlogsContainer;
    @FXML private VBox          myForumTopicsContainer;

    @FXML private Label lblLatestForum;
    @FXML private Label lblLatestGroup;
    @FXML private Label lblLatestCounselor;
    @FXML private Label lblLatestMessage;
    @FXML private Label lblLatestMeditation;
    @FXML private Label lblLatestJournal;

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
            


            loadFavorites(user);
            loadMyForumTopics(user);
            updateDynamicShortcuts(user);

        } else {
            lblGreeting.setText("Hos Geldin");
        }

        // Pick a random quote
        int idx = (int)(Math.random() * quotes.length);
        if (lblQuote != null) {
            lblQuote.setText("Günün Sözü: \"" + quotes[idx] + "\"");
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
            var songs = com.mindshield.services.MeditationPlaybackService.getInstance().getRecentTracks();
            
            // Header textini de güncelleyelim (Label hiyerarşide favSongsContainer'in üstünde ama ID'si yok,
            // bunu Home.fxml'de yaptık. Biz sadece container içini dolduralım).
            if (songs.isEmpty()) {
                Label empty = new Label("Henüz müzik dinlemediniz.");
                empty.setStyle("-fx-text-fill: #8A9CAE; -fx-font-size: 13px;");
                favSongsContainer.getChildren().add(empty);
            } else {
                for (var song : songs) {
                    Label lbl = new Label("Sarki: " + song.getTitle());
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



    private void updateDynamicShortcuts(BaseUser user) {
        // 1. Forum
        var topic = MainApp.forumService.getLatestTopicForUser(user);
        if (topic != null) {
            lblLatestForum.setText(topic.getTitle());
        } else {
            var allTopics = MainApp.forumService.getAllTopics();
            if (!allTopics.isEmpty()) lblLatestForum.setText(allTopics.get(0).getTitle());
        }
 
        // 2. Groups
        var room = MainApp.chatRoomService.getLatestRoomForUser(user);
        if (room != null) {
            lblLatestGroup.setText(room.getName());
        }

        // 3. Counselor
        var counselor = MainApp.messageService.getLatestConsultedCounselor(user);
        if (counselor != null) {
            lblLatestCounselor.setText(counselor.getPersona());
        }

        // 4. Message
        var partner = MainApp.messageService.getLatestConversationPartner(user);
        if (partner != null) {
            lblLatestMessage.setText(partner.getPersona());
        }

        // 5. Meditation
        var recentTracks = com.mindshield.services.MeditationPlaybackService.getInstance().getRecentTracks();
        if (!recentTracks.isEmpty()) {
            lblLatestMeditation.setText(recentTracks.get(0).getTitle());
        }

        // 6. Journal
        var journalEntries = MainApp.journalService.listAllMyEntries(user);
        if (!journalEntries.isEmpty()) {
            lblLatestJournal.setText(journalEntries.get(0).getTitle());
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

    @FXML
    private void navToCounselors() {
        DashboardController.getInstance().showCounselors();
    }
}
