package com.mindshield.ui;

import java.util.ArrayList;

import com.mindshield.models.MeditationTrack;
import com.mindshield.services.MeditationPlaybackService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;

public class MeditationController {

    @FXML
    private ListView<MeditationTrack> trackList;
    @FXML
    private Label lblNowPlaying;
    @FXML
    private Button btnPlay;
    @FXML
    private Button btnFavorite;
    @FXML
    private Slider volumeSlider;

    private final MeditationPlaybackService playback = MeditationPlaybackService.getInstance(); // Singleton olarak playback servisine erişiyoruz

    @FXML
    public void initialize() {
        loadLibrary();

        lblNowPlaying.textProperty().bind(playback.nowPlayingLineProperty()); // Şu an çalan parçayı göstermek için playback servisinden gelen metne bağlanıyoruz
        
        // Ses kontrolü için slider'ı playback servisinin volume özelliğine bağlıyoruz
        volumeSlider.setValue(playback.volumeProperty().get() * 100.0);
        volumeSlider.valueProperty()
                .addListener((obs, oldV, newV) -> playback.setVolumeFraction(newV.doubleValue() / 100.0)); 

        playback.playingProperty()
                .addListener((obs, was, playingNow) -> btnPlay.setText(Boolean.TRUE.equals(playingNow) ? "⏸ " : "▶ "));
        btnPlay.setText(playback.playingProperty().get() ? "⏸ " : "▶ ");

        syncListSelection();

        trackList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            updateFavoriteButton(newV);
        });

        // Listeden tek tıkla çalmaya çalışır
        trackList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                int idx = trackList.getSelectionModel().getSelectedIndex();
                if (idx >= 0) {
                    tryPlayIndex(idx);
                }
            }
        });
    }
    
    private void updateFavoriteButton(MeditationTrack track) {
        if (track == null || DashboardController.getCurrentUser() == null) {
            btnFavorite.setText("🤍");
            return;
        }
        boolean isFav = DashboardController.getCurrentUser().getFavoriteSongTitles().contains(track.getTitle());
        btnFavorite.setText(isFav ? "❤️" : "🤍");
    }

    // Meditasyon müziklerini yükler ve listeye ekler
    private void loadLibrary() {
        ArrayList<MeditationTrack> items = new ArrayList<>(playback.getTracks());
        trackList.setItems(FXCollections.observableArrayList(items));
    }

    // Playback servisi tarafından çalınan parçaya göre listede seçimi günceller
    private void syncListSelection() {
        int idx = playback.getCurrentIndex();
        if (idx >= 0 && idx < trackList.getItems().size()) {
            trackList.getSelectionModel().select(idx);
        }
    }

    // Belirtilen indeksteki parçayı çalmayı dener
    private void tryPlayIndex(int index) {
        if (!playback.playTrackAt(index)) {
            showAlert("Dosya Bulunamadı",
                    "Lütfen 'resources/audio' klasörü altına ilgili MP3 dosyasını ekleyin.");
        }
    }
    
    @FXML
    private void handlePlayPause() {
        if (!playback.hasActivePlayer()) {
            int idx = trackList.getSelectionModel().getSelectedIndex();
            if (idx < 0) {
                showAlert("Uyarı", "Lütfen çalmak için listeden bir müzik seçin.");
                return;
            }
            tryPlayIndex(idx);
            return;
        }
        playback.togglePauseResume();
    }

    @FXML
    private void handleStop() {
        playback.stopPlayback();
        btnPlay.setText("▶");
    }

    @FXML
    private void handlePrevious() {
        if (!playback.playPrevious()) {
            showAlert("Dosya Bulunamadı",
                    "Önceki parça için ses dosyası bulunamadı.");
        }
    }

    @FXML
    private void handleNext() {
        if (!playback.playNext()) {
            showAlert("Dosya Bulunamadı",
                    "Sonraki parça için ses dosyası bulunamadı.");
        }
    }

    @FXML
    private void handleFavorite() {
        MeditationTrack track = trackList.getSelectionModel().getSelectedItem();
        if (track == null) {
            showAlert("Uyarı", "Favoriye eklemek için listeden bir müzik seçin.");
            return;
        }
        com.mindshield.models.BaseUser user = DashboardController.getCurrentUser();
        if (user == null) return;
        
        java.util.List<String> favs = user.getFavoriteSongTitles();
        if (favs.contains(track.getTitle())) {
            favs.remove(track.getTitle());
        } else {
            favs.add(track.getTitle());
        }
        updateFavoriteButton(track);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
