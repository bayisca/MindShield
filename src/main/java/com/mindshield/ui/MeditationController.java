package com.mindshield.ui;

import java.util.ArrayList;

import com.mindshield.models.MeditationTrack;
import com.mindshield.services.MeditationPlaybackService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MeditationController {

    @FXML
    private ListView<MeditationTrack> trackList;
    @FXML
    private Label lblNowPlaying;
    @FXML
    private Button btnPlay;
    @FXML
    private Slider volumeSlider;
    @FXML
    private CheckBox chkShowFavorites;

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
                .addListener((obs, was, playingNow) -> btnPlay.setText(Boolean.TRUE.equals(playingNow) ? "Duraklat" : "Oynat"));
        btnPlay.setText(playback.playingProperty().get() ? "Duraklat" : "Oynat");

        syncListSelection();



        // Listeden tek tıkla çalmaya çalışır
        trackList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                MeditationTrack track = trackList.getSelectionModel().getSelectedItem();
                if (track != null) {
                    int globalIdx = playback.getTracks().indexOf(track);
                    if (globalIdx >= 0) {
                        tryPlayIndex(globalIdx);
                    }
                }
            }
        });
    }
    


    // Meditasyon müziklerini yükler ve listeye ekler
    private void loadLibrary() {
        boolean showFavs = chkShowFavorites != null && chkShowFavorites.isSelected();
        ArrayList<MeditationTrack> items = new ArrayList<>();
        com.mindshield.dao.MediaDao mediaDao = new com.mindshield.dao.MediaDaoImpl();
        com.mindshield.models.BaseUser user = DashboardController.getCurrentUser();
        for (MeditationTrack track : playback.getTracks()) {
            boolean isFav = user != null && mediaDao.isFavorite(user.getId(), track.getId());
            if (!showFavs || isFav) {
                items.add(track);
            }
        }
        trackList.setItems(FXCollections.observableArrayList(items));
        
        trackList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(MeditationTrack track, boolean empty) {
                super.updateItem(track, empty);
                if (empty || track == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label nameLbl = new Label(track.getTitle() + " - " + track.getAuthor());
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    Button favBtn = new Button();
                    favBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
                    boolean isFav = user != null && mediaDao.isFavorite(user.getId(), track.getId());
                    favBtn.setText(isFav ? "❤️" : "🤍");
                    
                    favBtn.setOnAction(e -> {
                        if (user != null) {
                            if (mediaDao.isFavorite(user.getId(), track.getId())) {
                                mediaDao.removeFavorite(user.getId(), track.getId());
                            } else {
                                mediaDao.addFavorite(user.getId(), track.getId());
                            }
                            boolean updatedFav = mediaDao.isFavorite(user.getId(), track.getId());
                            favBtn.setText(updatedFav ? "❤️" : "🤍");
                            if (chkShowFavorites != null && chkShowFavorites.isSelected() && !updatedFav) {
                                loadLibrary(); // Eğer sadece favoriler açıksa ve favoriden çıkarılırsa listeyi yenile
                            }
                        }
                    });
                    
                    box.getChildren().addAll(nameLbl, spacer, favBtn);
                    setText(null);
                    setGraphic(box);
                }
            }
        });
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
    private void handleToggleFavorites() {
        loadLibrary();
    }

    @FXML
    private void handlePlayPause() {
        if (!playback.hasActivePlayer()) {
            MeditationTrack track = trackList.getSelectionModel().getSelectedItem();
            if (track == null) {
                showAlert("Uyarı", "Lütfen çalmak için listeden bir müzik seçin.");
                return;
            }
            int globalIdx = playback.getTracks().indexOf(track);
            if (globalIdx >= 0) {
                tryPlayIndex(globalIdx);
            }
            return;
        }
        playback.togglePauseResume();
    }

    @FXML
    private void handleStop() {
        playback.stopPlayback();
        btnPlay.setText("Oynat");
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



    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
