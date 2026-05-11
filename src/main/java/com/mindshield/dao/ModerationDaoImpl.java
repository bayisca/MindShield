package com.mindshield.dao;

import com.mindshield.models.ModerationReport;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ModerationDaoImpl implements ModerationDao {

    @Override
    public void save(ModerationReport report) {
        String sql = "INSERT INTO moderation_reports (id, kind, reporter_persona, reported_persona, post_id, room_id, message_id, message_preview, reason, created_at, resolved) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getId());
            ps.setString(2, report.getKind().name());
            ps.setString(3, report.getReporterPersona());
            ps.setString(4, report.getReportedPersona());
            ps.setString(5, report.getPostId());
            ps.setString(6, report.getRoomId());
            ps.setString(7, report.getMessageId());
            ps.setString(8, report.getMessagePreview());
            ps.setString(9, report.getReason());
            ps.setTimestamp(10, Timestamp.valueOf(report.getCreatedAt()));
            ps.setBoolean(11, report.isResolved());
            ps.executeUpdate();
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    @Override
    public void update(ModerationReport report) {
        String sql = "UPDATE moderation_reports SET resolved = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, report.isResolved());
            ps.setString(2, report.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    @Override
    public ModerationReport findById(String id) {
        String sql = "SELECT * FROM moderation_reports WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReport(rs);
                }
            }
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
        return null;
    }

    @Override
    public List<ModerationReport> findAll() {
        List<ModerationReport> reports = new ArrayList<>();
        String sql = "SELECT * FROM moderation_reports ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reports.add(mapReport(rs));
            }
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
        return reports;
    }

    private ModerationReport mapReport(ResultSet rs) throws SQLException {
        ModerationReport.Kind kind = ModerationReport.Kind.valueOf(rs.getString("kind"));
        ModerationReport report = new ModerationReport(
                kind,
                rs.getString("reporter_persona"),
                rs.getString("reported_persona"),
                rs.getString("post_id"),
                rs.getString("room_id"),
                rs.getString("message_id"),
                rs.getString("message_preview"),
                rs.getString("reason")
        );
        report.setId(rs.getString("id"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        report.setResolved(rs.getBoolean("resolved"));
        return report;
    }
}
