package com.mindshield.ui;

import com.mindshield.models.MeditationTrack;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MeditationController {

    @FXML private ListView<MeditationTrack> trackList;
    @FXML private Label lblNowPlaying;
    @FXML private Button btnPlay;
    @FXML private Slider volumeSlider;

    private MediaPlayer mediaPlayer;
    private MeditationTrack currentTrack;
    private boolean isPlaying = false;

    @FXML
    public void initialize() {
        loadLibrary();

        // Çift tıklama ile şarkı seçme
        trackList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                MeditationTrack selected = trackList.getSelectionModel().getSelectedItem();
                if (selected != null && selected != currentTrack) {
                    playTrack(selected);
                }
            }
        });

        // Ses seviyesi değiştiğinde
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
            }
        });
    }

    private void loadLibrary() {
        List<MeditationTrack> tracks = new ArrayList<>();
        // Burada gerçek dosyalarınız olmalı. Şimdilik sistemin nasıl görüneceğini göstermek için dummy veriler giriyoruz.
        tracks.add(new MeditationTrack("1", "Derin Uyku", "AI Ses", "derin_uyku.mp3", "Uykuya dalmayı kolaylaştıran frekanslar."));
        tracks.add(new MeditationTrack("2", "Sabah Enerjisi", "MindShield AI", "sabah_enerjisi.mp3", "Güne enerjik başlamak için motivasyon sesi."));
        tracks.add(new MeditationTrack("3", "Stres Giderme", "Uzman Psikolog", "stres_giderme.mp3", "Yoğun stres anlarında dinlenmesi gereken meditasyon."));
        tracks.add(new MeditationTrack("4", "Odaklanma Müzigi", "AI Ses", "odaklanma.mp3", "Çalışırken veya ders çalışırken odaklanmayı artırır."));

        trackList.setItems(FXCollections.observableArrayList(tracks));
    }

    private void playTrack(MeditationTrack track) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // Dosyanın yolunu bul (Örn: src/main/resources/audio/derin_uyku.mp3)
            URL resource = getClass().getResource("/audio/" + track.getFilename());
            
            if (resource == null) {
                // Eğer ses dosyası gerçekten projede yoksa hata göster
                showAlert("Dosya Bulunamadı", "Lütfen 'resources/audio' klasörü altına '" + track.getFilename() + "' adlı dosyayı ekleyin.");
                return;
            }

            Media media = new Media(resource.toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
            
            // Müzik bittiğinde otomatik durması için
            mediaPlayer.setOnEndOfMedia(() -> {
                btnPlay.setText("▶ Oynat");
                isPlaying = false;
            });

            currentTrack = track;
            lblNowPlaying.setText("Şu An Çalan: " + track.getTitle());
            
            mediaPlayer.play();
            isPlaying = true;
            btnPlay.setText("⏸ Duraklat");

        } catch (Exception e) {
            showAlert("Çalma Hatası", "Dosya çalınırken bir hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) {
            MeditationTrack selected = trackList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                playTrack(selected);
            } else {
                showAlert("Uyarı", "Lütfen çalmak için listeden bir müzik seçin.");
            }
            return;
        }

        if (isPlaying) {
            mediaPlayer.pause();
            btnPlay.setText("▶ Oynat");
            isPlaying = false;
        } else {
            mediaPlayer.play();
            btnPlay.setText("⏸ Duraklat");
            isPlaying = true;
        }
    }

    @FXML
    private void handleStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            btnPlay.setText("▶ Oynat");
            isPlaying = false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
