package com.mindshield.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. FXML dosyasını yükle (resources klasöründen çeker)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/Dashboard.fxml"));
        
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