package com.mindshield.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override // İşte eksik olan sihirli kelime bu kanka!
    public void start(Stage primaryStage) {
        // Ekranın ortasına bir yazı koyalım
        Label label = new Label("MindShield Projesine Hos Geldiniz!");
        StackPane root = new StackPane();
        root.getChildren().add(label);

        // Pencere ayarları
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setTitle("MindShield - Anonim Destek");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}