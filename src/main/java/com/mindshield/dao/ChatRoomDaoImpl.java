package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import com.mindshield.models.ChatRoom;
import com.mindshield.models.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomDaoImpl implements ChatRoomDao {
    private final UserDao userDao;

    public ChatRoomDaoImpl() {
        this.userDao = new UserDaoImpl();
    }

    public ChatRoomDaoImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void save(ChatRoom room) {
        String sql = "INSERT INTO chatrooms (id, room_name, is_private) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getId());
            ps.setString(2, room.getName());
            ps.setBoolean(3, !room.isActive());
            ps.executeUpdate();
            updateMembers(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(ChatRoom room) {
        String sql = "UPDATE chatrooms SET room_name = ?, is_private = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getName());
            ps.setBoolean(2, !room.isActive());
            ps.setString(3, room.getId());
            ps.executeUpdate();
            updateMembers(room);
            updateMessages(room);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateMembers(ChatRoom room) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete old members
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM chatroom_members WHERE room_id = ?")) {
                del.setString(1, room.getId());
                del.executeUpdate();
            }
            // Insert current members
            try (PreparedStatement ins = conn.prepareStatement("INSERT INTO chatroom_members (room_id, user_id) VALUES (?, ?)")) {
                for (BaseUser user : room.getMembers()) {
                    ins.setString(1, room.getId());
                    ins.setString(2, user.getId());
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    private void updateMessages(ChatRoom room) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // This is a bit complex as we only want to add new messages.
            // For simplicity in this demo, let's just insert missing ones.
            String sql = "INSERT INTO chatroomMessages (id, room_id, sender_id, content, created_at) SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM chatroomMessages WHERE id = ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (ChatMessage msg : room.getMessages()) {
                    ps.setString(1, msg.getId());
                    ps.setString(2, room.getId());
                    ps.setString(3, msg.getSender().getId());
                    ps.setString(4, msg.getContent());
                    ps.setTimestamp(5, Timestamp.valueOf(msg.getTimestamp()));
                    ps.setString(6, msg.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM chatroomMessages WHERE room_id = ?")) {
                ps1.setString(1, id);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM chatroom_members WHERE room_id = ?")) {
                ps2.setString(1, id);
                ps2.executeUpdate();
            }
            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM chatrooms WHERE id = ?")) {
                ps3.setString(1, id);
                ps3.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ChatRoom findById(String id) {
        String sql = "SELECT * FROM chatrooms WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ChatRoom> findAll() {
        List<ChatRoom> rooms = new ArrayList<>();
        String sql = "SELECT * FROM chatrooms";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                rooms.add(mapRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private ChatRoom mapRoom(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String name = rs.getString("room_name");
        boolean isPrivate = rs.getBoolean("is_private");
        
        ChatRoom room = new ChatRoom(name, "Support Group", null); // Topic is not in schema, placeholder
        room.setId(id);
        if (isPrivate) room.deactivate(); // Placeholder logic for private
        
        // Load members
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM chatroom_members WHERE room_id = ?")) {
            ps.setString(1, id);
            try (ResultSet mrs = ps.executeQuery()) {
                while (mrs.next()) {
                    BaseUser user = userDao.findById(mrs.getString("user_id"));
                    if (user != null) room.addMember(user);
                }
            }
        }

        // Load messages
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM chatroomMessages WHERE room_id = ? ORDER BY created_at ASC")) {
            ps.setString(1, id);
            try (ResultSet mrs = ps.executeQuery()) {
                while (mrs.next()) {
                    BaseUser sender = userDao.findById(mrs.getString("sender_id"));
                    if (sender != null) {
                        ChatMessage msg = new ChatMessage(id, sender, mrs.getString("content"));
                        msg.setId(mrs.getString("id"));
                        msg.setTimestamp(mrs.getTimestamp("created_at").toLocalDateTime());
                        room.addMessage(msg);
                    }
                }
            }
        }

        return room;
    }
}