package com.mindshield.ui;

import java.util.HashMap;
import java.util.Map;

import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static Map<String, BaseUser> userDatabase = new HashMap<>();
    public static Map<String, String> chatDatabase = new HashMap<>();
    public static com.mindshield.services.PostService postService = new com.mindshield.services.PostService();
    public static com.mindshield.services.MessageService messageService = new com.mindshield.services.MessageService();
    public static com.mindshield.services.JournalService journalService = new com.mindshield.services.JournalService();

    static {
        // Default admin counselor (pre-approved)
        Counselor admin = new Counselor("admin", "admin-001", "admin123", "Genel Psikoloji");
        admin.setApproved(true);
        userDatabase.put("admin", admin);

        // Sample counselor for testing
        Counselor demoDoc = new Counselor("Dr.Ayse", "doc-002", "doc123", "Anksiyete ve Stres");
        demoDoc.setApproved(true);
        userDatabase.put("Dr.Ayse", demoDoc);

        // Seed sample blog posts through postService (so they appear in the blog list)
        postService.seedSamplePosts(admin);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. FXML dosyasını yükle (resources klasöründen çeker)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));

        Parent root = loader.load();

        // 2. Sahneyi (Scene) oluştur
        Scene scene = new Scene(root);

        // 3. Window settings
        primaryStage.setTitle("MindShield — Anonim Danışmanlık");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(640);
        primaryStage.setResizable(true);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}