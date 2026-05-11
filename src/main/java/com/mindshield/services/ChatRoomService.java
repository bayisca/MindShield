package com.mindshield.services;

import java.sql.*;
import com.mindshield.dao.DatabaseConnection;
import com.mindshield.dao.ChatRoomDao;
import com.mindshield.dao.ChatRoomDaoImpl;
import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.ChatMessage;
import com.mindshield.models.ChatRoom;
import com.mindshield.ui.UserRole;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChatRoomService {
    private final ChatRoomDao chatRoomDao;

    public ChatRoomService() {
        this.chatRoomDao = new ChatRoomDaoImpl();
    }

    public ChatRoomService(ChatRoomDao chatRoomDao) {
        this.chatRoomDao = chatRoomDao;
    }

    // --- Yetki Kontrolleri ---

    private void enforceAdmin(BaseUser user) {
        if (user == null || user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Bu işlem yalnızca yöneticilere özeldir.");
        }
    }

    private void enforceMember(BaseUser user, ChatRoom room) {
        if (!room.isMember(user)) {
            throw new UnauthorizedException("Bu işlem için odanın üyesi olmalısınız.");
        }
    }

    private void enforceCanJoin(BaseUser user) {
        if (user == null) {
            throw new UnauthorizedException("Odaya katılmak için giriş yapmalısınız.");
        }
        UserRole role = user.getRole();
        if (role != UserRole.CLIENT && role != UserRole.ANONYMOUS && role != UserRole.COUNSELOR) {
            throw new UnauthorizedException(
                    "Grup sohbetleri yalnızca danışan ve danışman hesapları içindir.");
        }
    }

    private ChatRoom findActiveRoom(String roomId) {
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        if (!room.isActive()) {
            throw new IllegalStateException("Bu oda yönetici tarafından kapatılmıştır.");
        }
        return room;
    }

    // --- Admin İşlemleri ---

    public ChatRoom createRoom(BaseUser admin, String name, String topic) {
        enforceAdmin(admin);
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Oda adı boş olamaz.");
        }
        if (topic == null || topic.trim().isEmpty()) {
            throw new IllegalArgumentException("Oda konusu boş olamaz.");
        }
        ChatRoom room = new ChatRoom(name.trim(), topic.trim(), admin);
        chatRoomDao.save(room);
        return room;
    }

    public void deleteRoom(BaseUser admin, String roomId) {
        enforceAdmin(admin);
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        chatRoomDao.deleteById(roomId);
    }

    public void deactivateRoom(BaseUser admin, String roomId) {
        enforceAdmin(admin);
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        room.deactivate();
        chatRoomDao.update(room);
    }

    public void activateRoom(BaseUser admin, String roomId) {
        enforceAdmin(admin);
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        room.activate();
        chatRoomDao.update(room);
    }

    public void kickMember(BaseUser admin, String roomId, BaseUser target) {
        enforceAdmin(admin);
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        if (!room.isMember(target)) {
            throw new IllegalArgumentException("Bu kullanıcı odada bulunmuyor.");
        }
        room.removeMember(target);
        chatRoomDao.update(room);
    }

    // --- Üye İşlemleri ---

    public void joinRoom(BaseUser user, String roomId) {
        enforceCanJoin(user);
        ChatRoom room = findActiveRoom(roomId);
        if (room.isMember(user)) {
            throw new IllegalStateException("Zaten bu odanın üyesisiniz.");
        }
        room.addMember(user);
        chatRoomDao.update(room);
    }

    public void leaveRoom(BaseUser user, String roomId) {
        ChatRoom room = findActiveRoom(roomId);
        enforceMember(user, room);
        room.removeMember(user);
        chatRoomDao.update(room);
    }

    // --- Mesaj İşlemleri ---

    public ChatMessage sendMessage(BaseUser sender, String roomId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Mesaj boş olamaz.");
        }
        ChatRoom room = findActiveRoom(roomId);
        enforceMember(sender, room);
        ChatMessage message = new ChatMessage(roomId, sender, content.trim());
        room.addMessage(message);
        chatRoomDao.update(room);
        return message;
    }

    // --- Listeleme ---

    public List<ChatRoom> getAllRooms() {
        return chatRoomDao.findAll();
    }

    public List<ChatRoom> getActiveRooms() {
        return chatRoomDao.findAll().stream()
                .filter(ChatRoom::isActive)
                .collect(Collectors.toList());
    }

    public ChatRoom findRoomById(String roomId) {
        ChatRoom room = chatRoomDao.findById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Oda bulunamadı.");
        }
        return room;
    }

    /** Kullanıcıyı tüm odalardan çıkarır (hesap silme / moderasyon). */
    public void removeUserFromAllRooms(BaseUser user) {
        if (user == null) {
            return;
        }
        for (ChatRoom room : new ArrayList<>(chatRoomDao.findAll())) {
            if (room != null && room.isMember(user)) {
                room.removeMember(user);
                chatRoomDao.update(room);
            }
        }
    }

    /** Ada göre aktif destek odası arar; yoksa null. */
    public ChatRoom findActiveRoomByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return chatRoomDao.findAll().stream()
                .filter(r -> r != null && r.isActive() && name.equals(r.getName()))
                .findFirst()
                .orElse(null);
    }

    public ChatRoom getLatestRoomForUser(BaseUser user) {
        if (user == null) return null;
        String sql = """
            SELECT room_id FROM chatroomMessages 
            WHERE sender_id = ? 
            ORDER BY created_at DESC 
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return chatRoomDao.findById(rs.getString("room_id"));
                }
            }
        } catch (SQLException e) {
            com.mindshield.util.AppLog.severe(e);
        }
        // Fallback to first joined room
        return chatRoomDao.findAll().stream()
                .filter(r -> r != null && r.isActive() && r.isMember(user))
                .findFirst()
                .orElse(null);
    }
}