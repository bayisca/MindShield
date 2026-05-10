package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.MainApp;
import com.mindshield.ui.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public void save(BaseUser user) {
        if (user == null || user.getPersona() == null) {
            throw new IllegalArgumentException("User or Persona cannot be null");
        }
        String sql = "MERGE INTO users (id, username, password, role, profession) KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getPersona());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, (user instanceof Counselor c) ? c.getSpecialization() : null);
            ps.executeUpdate();
            MainApp.userDatabase.put(user.getPersona(), user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BaseUser findByPersona(String persona) {
        if (MainApp.userDatabase.containsKey(persona)) {
            return MainApp.userDatabase.get(persona);
        }
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, persona);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BaseUser user = mapUser(rs);
                    MainApp.userDatabase.put(persona, user);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public BaseUser findById(String id) {
        // Check memory first
        for (BaseUser u : MainApp.userDatabase.values()) {
            if (u.getId().equals(id)) return u;
        }
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BaseUser user = mapUser(rs);
                    MainApp.userDatabase.put(user.getPersona(), user);
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean existsByPersona(String persona) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, persona);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<BaseUser> findAll() {
        List<BaseUser> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void deleteByPersona(String persona) {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, persona);
            ps.executeUpdate();
            MainApp.userDatabase.remove(persona);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private BaseUser mapUser(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        UserRole role = UserRole.valueOf(rs.getString("role"));
        String profession = rs.getString("profession");

        if (role == UserRole.COUNSELOR) {
            return new Counselor(id, username, password, profession);
        } else {
            return new StandardUser(username, id, password, role);
        }
    }
}
