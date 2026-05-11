package com.mindshield.dao;

import com.mindshield.models.SystemLog;
import com.mindshield.models.Report;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Admin;
import com.mindshield.models.Counselor;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SystemLogDaoImpl implements SystemLogDao {

    @Override
    public void saveLog(SystemLog log) {
        String sql = "INSERT INTO system_logs (id, action, created_at) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, log.getId());
            ps.setString(2, log.getAction());
            ps.setTimestamp(3, Timestamp.valueOf(log.getTimestamp()));
            ps.executeUpdate();
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    @Override
    public List<SystemLog> findAllLogs() {
        List<SystemLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM system_logs ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                SystemLog log = new SystemLog(rs.getString("id"), rs.getString("action"));
                log.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());
                logs.add(log);
            }
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
        return logs;
    }

    @Override
    public void saveReport(Report report) {
        String sql = "INSERT INTO general_reports (id, reporter_id, content_id, reason, created_at) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getId());
            ps.setString(2, report.getReporter().getId());
            ps.setString(3, report.getContentId());
            ps.setString(4, report.getReason());
            ps.setTimestamp(5, Timestamp.valueOf(report.getTimestamp()));
            ps.executeUpdate();
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
    }

    @Override
    public List<Report> findAllReports() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT r.*, u.username, u.password, u.role, u.profession FROM general_reports r JOIN users u ON r.reporter_id = u.id ORDER BY r.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BaseUser reporter = mapUser(rs);
                Report report = new Report(rs.getString("id"), reporter, rs.getString("content_id"), rs.getString("reason"));
                report.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());
                reports.add(report);
            }
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
        return reports;
    }

    private BaseUser mapUser(ResultSet rs) throws SQLException {
        String id = rs.getString("reporter_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        String profession = rs.getString("profession");

        if (role == UserRole.COUNSELOR) {
            return new Counselor(id, username, password, profession);
        } else if (role == UserRole.ADMIN) {
            return new Admin(username, id, password);
        } else {
            return new StandardUser(id, username, password, role);
        }
    }
}
