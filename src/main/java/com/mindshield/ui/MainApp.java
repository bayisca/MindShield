package com.mindshield.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainApp extends Application {

    public static java.util.Map<String, com.mindshield.models.BaseUser> userDatabase = new java.util.HashMap<>();
    public static java.util.List<com.mindshield.models.BlogPost> blogPosts = new java.util.ArrayList<>();
    public static java.util.Map<String, String> chatDatabase = new java.util.HashMap<>();

    static {
        // Default admin (Counselor)
        com.mindshield.models.BaseUser admin = new com.mindshield.models.Counselor("admin", "admin-001", "admin123", "General Wellness");
        userDatabase.put("admin", admin);

        // Sample Blog Posts
        blogPosts.add(new com.mindshield.models.BlogPost(admin, "Stresle Baş Etme Yolları", "Modern yaşamın getirdiği stresi yönetmek için bilimsel yöntemler ve günlük egzersizler..."));
        blogPosts.add(new com.mindshield.models.BlogPost(admin, "Anksiyete ve Sosyal Fobi", "Sosyal ortamlarda rahat hissetmek için uygulayabileceğiniz bilişsel davranışçı teknikler."));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. FXML dosyasını yükle (resources klasöründen çeker)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
    
        Parent root = loader.load();

        // 2. Sahneyi (Scene) oluştur
        Scene scene = new Scene(root);
        
        // 3. Pencere ayarları
        primaryStage.setTitle("MindShield - Anonim Giriş");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Tasarımın bozulmaması için pencereyi sabitleyebilirsin
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}