package com.mindshield.ui;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


import java.util.List;
import java.util.stream.Collectors;

public class SettingsController {
    @FXML private TextField newPersonaName;
    @FXML private PasswordField newPassword;
    @FXML private VBox myPostsContainer;

    @FXML
    public void initialize() {
        BaseUser user = DashboardController.getCurrentUser();
        if (user != null) {
            newPersonaName.setText(user.getPersona());
            if (user.getRole() == UserRole.COUNSELOR || user.getRole() == UserRole.SUPERADMIN) {
                loadMyPosts(user);
            } else {
                myPostsContainer.setVisible(false);
                myPostsContainer.setManaged(false);
            }
        }
    }

    private void loadMyPosts(BaseUser user) {
        if (myPostsContainer == null) return;
        
        myPostsContainer.getChildren().clear();
        
        List<BlogPost> myPosts = MainApp.blogPosts.stream()
                .filter(p -> p.getAuthor().getPersona().equals(user.getPersona()))
                .collect(Collectors.toList());

        if (myPosts.isEmpty()) {
            myPostsContainer.getChildren().add(new Label("Henüz bir yazınız bulunmuyor."));
        } else {
            for (BlogPost post : myPosts) {
                myPostsContainer.getChildren().add(createMiniCard(post));
            }
        }
    }

    private VBox createMiniCard(BlogPost post) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #eee; -fx-border-radius: 5;");
        
        Label title = new Label(post.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        Label date = new Label(post.getCreatedAt().toString().substring(0, 10));
        date.setTextFill(Color.GRAY);
        date.setFont(Font.font(10));

        card.getChildren().addAll(title, date);
        
        // Detaya gitme imkanı da verelim
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> {
            DashboardController.getInstance().showBlogDetail(post);
        });

        return card;
    }

    @FXML 
    private void saveSettings() { 
        BaseUser user = DashboardController.getCurrentUser();
        if (user != null) {
            // Basit simülasyon: Kullanıcı adını güncelle (şifreyi de geçebiliriz)
            System.out.println("Ayarlar kaydedildi: " + newPersonaName.getText()); 
            // Gerçek veri modelinde güncelleme yapmak için MainApp.userDatabase'i de güncellemek gerekebilir.
        }
    }
}