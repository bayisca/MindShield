package com.mindshield.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Counselor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    public static Map<String, BaseUser> userDatabase = new HashMap<>();
    public static List<BlogPost> blogPosts = new ArrayList<>();
    public static Map<String, String> chatDatabase = new HashMap<>();
    public static com.mindshield.services.PostService postService = new com.mindshield.services.PostService();
    public static com.mindshield.services.MessageService messageService = new com.mindshield.services.MessageService();

    static {
        // Default admin (Counselor)
        BaseUser admin = new Counselor("admin", "admin-001", "admin123", "General Wellness");
        userDatabase.put("admin", admin);

        // Sample Blog Posts
        blogPosts.add(new BlogPost(admin, "Stresle Baş Etme Yolları", "Modern yaşamın getirdiği stresi yönetmek için bilimsel yöntemler ve günlük egzersizler..."));
        blogPosts.add(new BlogPost(admin, "Anksiyete ve Sosyal Fobi", "Sosyal ortamlarda rahat hissetmek için uygulayabileceğiniz bilişsel davranışçı teknikler."));
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