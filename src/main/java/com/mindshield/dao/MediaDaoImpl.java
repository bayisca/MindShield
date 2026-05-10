package com.mindshield.dao;

import com.mindshield.models.MeditationTrack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MediaDaoImpl implements MediaDao {

    @Override
    public void saveTrack(MeditationTrack track) {
        String sql = "MERGE INTO media (id, title, filename, description) KEY(id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, track.getId());
            ps.setString(2, track.getTitle());
            ps.setString(3, track.getFilename());
            ps.setString(4, track.getDescription());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MeditationTrack> getAllTracks() {
        List<MeditationTrack> tracks = new ArrayList<>();
        String sql = "SELECT * FROM media";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tracks.add(mapTrack(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tracks;
    }

    @Override
    public boolean isFavorite(String userId, String trackId) {
        String sql = "SELECT 1 FROM favorite_songs WHERE user_id = ? AND media_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, trackId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addFavorite(String userId, String trackId) {
        if (isFavorite(userId, trackId)) return;
        String sql = "INSERT INTO favorite_songs (id, user_id, media_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.setString(3, trackId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFavorite(String userId, String trackId) {
        String sql = "DELETE FROM favorite_songs WHERE user_id = ? AND media_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, trackId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MeditationTrack> getFavoriteTracks(String userId) {
        List<MeditationTrack> tracks = new ArrayList<>();
        String sql = "SELECT m.* FROM media m JOIN favorite_songs f ON m.id = f.media_id WHERE f.user_id = ? ORDER BY f.added_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapTrack(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tracks;
    }

    @Override
    public void addRecentTrack(String userId, String trackId) {
        // First remove if it already exists in recent to avoid duplicates, then insert as new to update the played_at timestamp
        String deleteSql = "DELETE FROM recent_songs WHERE user_id = ? AND media_id = ?";
        String insertSql = "INSERT INTO recent_songs (id, user_id, media_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement deletePs = conn.prepareStatement(deleteSql)) {
                deletePs.setString(1, userId);
                deletePs.setString(2, trackId);
                deletePs.executeUpdate();
            }
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, UUID.randomUUID().toString());
                insertPs.setString(2, userId);
                insertPs.setString(3, trackId);
                insertPs.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<MeditationTrack> getRecentTracks(String userId, int limit) {
        List<MeditationTrack> tracks = new ArrayList<>();
        String sql = "SELECT m.* FROM media m JOIN recent_songs r ON m.id = r.media_id WHERE r.user_id = ? ORDER BY r.played_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapTrack(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tracks;
    }

    private MeditationTrack mapTrack(ResultSet rs) throws SQLException {
        return new MeditationTrack(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("filename"),
                rs.getString("description")
        );
    }
}
