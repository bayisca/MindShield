package com.mindshield.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mindshield.models.MeditationTrack;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import com.mindshield.dao.MediaDao;
import com.mindshield.dao.MediaDaoImpl;
import com.mindshield.ui.DashboardController;
import com.mindshield.models.BaseUser;

/**
 * Sekme değişiminde controller yeniden oluşturulsa bile çalmayı sürdürmek için
 * MediaPlayer tek örnekte tutulur.
 */
public final class MeditationPlaybackService {

    private static final MeditationPlaybackService INSTANCE = new MeditationPlaybackService();

    public static MeditationPlaybackService getInstance() {
        return INSTANCE;
    }

    private final List<MeditationTrack> tracks = new ArrayList<>();
    private boolean tracksLoaded = false;
    private MediaPlayer mediaPlayer;
    private int currentIndex = -1;

    private final DoubleProperty volume = new SimpleDoubleProperty(0.5);
    private final StringProperty nowPlayingLine = new SimpleStringProperty("Şu An Çalan: (Seçim Yapılmadı)");
    private final ReadOnlyBooleanWrapper playing = new ReadOnlyBooleanWrapper(false);

    private final MediaDao mediaDao = new MediaDaoImpl();

    private MeditationPlaybackService() {
        // Track yüklemesi lazy olarak getTracks() içinde yapılır
    }

    private void ensureTracksLoaded() {
        if (!tracksLoaded) {
            tracks.addAll(mediaDao.getAllTracks());
            tracksLoaded = true;
        }
    }

    public List<MeditationTrack> getTracks() {
        ensureTracksLoaded();
        return Collections.unmodifiableList(tracks);
    }

    public List<MeditationTrack> getRecentTracks(String userId) {
        if (userId == null) return Collections.emptyList();
        return mediaDao.getRecentTracks(userId, 3);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void addTrack(MeditationTrack track) {
        ensureTracksLoaded();
        mediaDao.saveTrack(track);
        tracks.add(track);
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public StringProperty nowPlayingLineProperty() {
        return nowPlayingLine;
    }

    public ReadOnlyBooleanProperty playingProperty() {
        return playing.getReadOnlyProperty();
    }

    public void setVolumeFraction(double v) {
        volume.set(Math.max(0, Math.min(1, v)));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume.get());
        }
    }

    /**
     * @return false if audio file missing or load error
     */
    public boolean playTrackAt(int index) {
        if (index < 0 || index >= tracks.size()) {
            return false;
        }
        MeditationTrack track = tracks.get(index);
        String sourceUrl;
        if (track.getFilename().startsWith("http://") || track.getFilename().startsWith("https://")) {
            sourceUrl = track.getFilename();
        } else {
            URL resource = MeditationPlaybackService.class.getResource("/audio/" + track.getFilename());
            if (resource == null) {
                return false;
            }
            sourceUrl = resource.toExternalForm();
        }

        if (mediaPlayer != null) {
            mediaPlayer.statusProperty().removeListener(this::onStatusChanged);
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }

        try {
            Media media = new Media(sourceUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume.get());
            currentIndex = index;

            mediaPlayer.statusProperty().addListener(this::onStatusChanged);
            mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
                playing.set(false);
                nowPlayingLine.set("Şu An Çalan: " + track.getTitle() + " (bitti)");
            }));

            mediaPlayer.play();
            nowPlayingLine.set("Şu An Çalan: " + track.getTitle());
            
            BaseUser user = DashboardController.getCurrentUser();
            if (user != null) {
                mediaDao.addRecentTrack(user.getId(), track.getId());
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            currentIndex = -1;
            nowPlayingLine.set("Şu An Çalan: (Seçim Yapılmadı)");
            playing.set(false);
            return false;
        }
    }

    private void onStatusChanged(javafx.beans.value.ObservableValue<? extends MediaPlayer.Status> obs,
                                 MediaPlayer.Status oldV,
                                 MediaPlayer.Status newV) {
        Platform.runLater(() -> playing.set(newV == MediaPlayer.Status.PLAYING));
    }

    public void togglePauseResume() {
        if (mediaPlayer == null) {
            return;
        }
        MediaPlayer.Status st = mediaPlayer.getStatus();
        if (st == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.play();
        }
    }

    public void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.statusProperty().removeListener(this::onStatusChanged);
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        playing.set(false);
        currentIndex = -1;
        nowPlayingLine.set("Şu An Çalan: (Seçim Yapılmadı)");
    }

    public boolean playNext() {
        if (tracks.isEmpty()) return false;
        int next = currentIndex < 0 ? 0 : (currentIndex + 1) % tracks.size();
        return playTrackAt(next);
    }

    public boolean playPrevious() {
        if (tracks.isEmpty()) return false;
        int prev = currentIndex < 0 ? tracks.size() - 1 : (currentIndex - 1 + tracks.size()) % tracks.size();
        return playTrackAt(prev);
    }

    public boolean hasActivePlayer() {
        return mediaPlayer != null;
    }
}
