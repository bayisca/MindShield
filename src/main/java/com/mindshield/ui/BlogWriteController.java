package com.mindshield.ui;

import com.mindshield.models.BlogPost;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class BlogWriteController {
    @FXML private Button btnCancel;
    @FXML private Button btnPublish;
    @FXML private TextField fldTitle;
    @FXML private TextArea fldContent;
    @FXML private Label lblAuthor;

    private BlogPost editingPost;

    @FXML
    public void initialize() {
        var user = DashboardController.getCurrentUser();
        if (user != null) {
            lblAuthor.setText(user.getPersona());
        }

        btnCancel.setOnAction(e -> {
            if (editingPost != null) {
                DashboardController.getInstance().showBlogDetail(editingPost);
            } else {
                DashboardController.getInstance().showBlog();
            }
        });
        
        btnPublish.setOnAction(e -> publishContent());
    }

    public void setEditPost(BlogPost post) {
        this.editingPost = post;
        fldTitle.setText(post.getTitle());
        fldContent.setText(post.getBody());
        btnPublish.setText("Değişiklikleri Kaydet");
    }

    private void publishContent() {
        String title = fldTitle.getText();
        String body = fldContent.getText();

        if (title != null && !title.isEmpty() && body != null && !body.isEmpty()) {
            if (editingPost != null) {
                // Düzenleme modu
                editingPost.setTitle(title);
                editingPost.setBody(body);
                DashboardController.getInstance().showBlogDetail(editingPost);
            } else {
                // Yeni yazı modu
                var user = DashboardController.getCurrentUser();
                if (user == null) user = MainApp.userDatabase.get("admin");

                BlogPost newPost = new BlogPost(user, title, body);
                MainApp.blogPosts.add(0, newPost); 
                DashboardController.getInstance().showBlog();
            }
        } else {
            System.out.println("Hata: Başlık ve içerik boş olamaz.");
        }
    }
}
