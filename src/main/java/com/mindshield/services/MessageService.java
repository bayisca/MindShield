package com.mindshield.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Message;

public class MessageService {

    public Message sendMessage(BaseUser sender, BaseUser receiver, String content) {

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String conversationId = findOrCreateConversation(conn, sender.getId(), receiver.getId());

            String id = UUID.randomUUID().toString();

            PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO DMmessages (
                    id,
                    conversation_id,
                    sender_id,
                    content
                )
                VALUES (?, ?, ?, ?)
            """);

            ps.setString(1, id);
            ps.setString(2, conversationId);
            ps.setString(3, sender.getId());
            ps.setString(4, content);

            ps.executeUpdate();

            return new Message(sender, receiver, content);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Message send failed");
        }
    }

    public List<Message> getMessagesBetween(BaseUser user1, BaseUser user2) {

        List<Message> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                SELECT m.*, c.user1_id, c.user2_id
                FROM DMmessages m
                JOIN conversations c ON m.conversation_id = c.id
                WHERE (c.user1_id = ? AND c.user2_id = ?)
                   OR (c.user1_id = ? AND c.user2_id = ?)
                ORDER BY m.sent_at ASC
            """);

            ps.setString(1, user1.getId());
            ps.setString(2, user2.getId());
            ps.setString(3, user2.getId());
            ps.setString(4, user1.getId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                list.add(new Message(
                        user1.getId().equals(rs.getString("sender_id")) ? user1 : user2,
                        user1.getId().equals(rs.getString("sender_id")) ? user2 : user1,
                        rs.getString("content")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public boolean hasChatBetween(BaseUser user1, BaseUser user2) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                SELECT 1 FROM conversations
                WHERE (user1_id = ? AND user2_id = ?)
                   OR (user1_id = ? AND user2_id = ?)
                LIMIT 1
            """);

            ps.setString(1, user1.getId());
            ps.setString(2, user2.getId());
            ps.setString(3, user2.getId());
            ps.setString(4, user1.getId());

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void purgeInvolvingPersona(String userId) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                DELETE FROM DMmessages
                WHERE sender_id = ?
                   OR conversation_id IN (
                        SELECT id FROM conversations
                        WHERE user1_id = ? OR user2_id = ?
                   )
            """);

            ps.setString(1, userId);
            ps.setString(2, userId);
            ps.setString(3, userId);

            ps.executeUpdate();

            PreparedStatement ps2 = conn.prepareStatement("""
                DELETE FROM conversations
                WHERE user1_id = ? OR user2_id = ?
            """);

            ps2.setString(1, userId);
            ps2.setString(2, userId);

            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getHelpedClientsCount(BaseUser counselor) {
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("""
                SELECT COUNT(id) FROM conversations
                WHERE user1_id = ? OR user2_id = ?
            """);
            ps.setString(1, counselor.getId());
            ps.setString(2, counselor.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    private String findOrCreateConversation(Connection conn, String u1, String u2) throws Exception {

        PreparedStatement ps = conn.prepareStatement("""
            SELECT id FROM conversations
            WHERE (user1_id = ? AND user2_id = ?)
               OR (user1_id = ? AND user2_id = ?)
        """);

        ps.setString(1, u1);
        ps.setString(2, u2);
        ps.setString(3, u2);
        ps.setString(4, u1);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) return rs.getString("id");

        String id = UUID.randomUUID().toString();

        PreparedStatement ins = conn.prepareStatement("""
            INSERT INTO conversations (id, user1_id, user2_id)
            VALUES (?, ?, ?)
        """);

        ins.setString(1, id);
        ins.setString(2, u1);
        ins.setString(3, u2);

        ins.executeUpdate();

        return id;
    }
}