package com.mindshield.dao;

import java.util.List;
import com.mindshield.models.MeditationTrack;

public interface MediaDao {
    void saveTrack(MeditationTrack track);
    List<MeditationTrack> getAllTracks();
    
    boolean isFavorite(String userId, String trackId);
    void addFavorite(String userId, String trackId);
    void removeFavorite(String userId, String trackId);
    List<MeditationTrack> getFavoriteTracks(String userId);

    void addRecentTrack(String userId, String trackId);
    List<MeditationTrack> getRecentTracks(String userId, int limit);
}
