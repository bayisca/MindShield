package com.mindshield.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.models.Admin;
import com.mindshield.models.BaseUser;
import com.mindshield.models.Counselor;
import com.mindshield.models.ForumCategory;
import com.mindshield.models.ForumReply;
import com.mindshield.models.ForumTopic;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.MainApp;
import com.mindshield.ui.UserRole;

public class ForumService {

    public List<ForumCategory> getCategories() {
        List<ForumCategory> categories = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        SELECT c.id, c.name, c.description, COUNT(t.id) AS topic_count
                        FROM forum_categories c
                        LEFT JOIN forum_topics t ON t.category_id = c.id
                        GROUP BY c.id, c.name, c.description, c.created_at
                        ORDER BY c.created_at, c.name
                        """);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(new ForumCategory(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getInt("topic_count")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<ForumTopic> getAllTopics() {
        return searchTopics(null, "");
    }

    public List<ForumTopic> searchTopics(String query) {
        return searchTopics(null, query);
    }

    public List<ForumTopic> getTopicsByCategory(String categoryId, String query) {
        return searchTopics(categoryId, query);
    }

    public List<ForumTopic> getTopicsByAuthor(BaseUser author) {
        if (author == null) {
            return List.of();
        }
        List<ForumTopic> topics = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        SELECT t.*, c.name AS category_name,
                               (SELECT COUNT(*) FROM forum_replies r WHERE r.topic_id = t.id) AS reply_count
                        FROM forum_topics t
                        JOIN forum_categories c ON c.id = t.category_id
                        WHERE t.user_id = ?
                        ORDER BY t.is_pinned DESC, t.created_at DESC
                        """)) {
            ps.setString(1, author.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(buildTopic(rs, false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }

    public ForumTopic createTopic(BaseUser author, String categoryId, String title, String body) {
        if (author == null) {
            throw new IllegalArgumentException("Baslik acmak icin giris yapmalisiniz.");
        }
        validateText(categoryId, "Kategori");
        validateText(title, "Baslik");
        validateText(body, "Icerik");

        String topicId = UUID.randomUUID().toString();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO forum_topics (id, category_id, user_id, title, content)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
            ps.setString(1, topicId);
            ps.setString(2, categoryId);
            ps.setString(3, author.getId());
            ps.setString(4, title.trim());
            ps.setString(5, body.trim());
            ps.executeUpdate();
            return findTopicById(topicId).orElseThrow(() -> new IllegalArgumentException("Baslik olusturulamadi."));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Baslik veritabanina kaydedilemedi.");
        }
    }

    public ForumTopic createTopic(BaseUser author, String title, String body) {
        String categoryId = getCategories().stream()
                .findFirst()
                .map(ForumCategory::getId)
                .orElseThrow(() -> new IllegalArgumentException("Forum kategorisi bulunamadi."));
        return createTopic(author, categoryId, title, body);
    }

    public ForumReply addReply(BaseUser author, String topicId, String body) {
        if (author == null) {
            throw new IllegalArgumentException("Yanit yazmak icin giris yapmalisiniz.");
        }
        validateText(topicId, "Baslik");
        validateText(body, "Yanit");
        if (findTopicById(topicId).isEmpty()) {
            throw new IllegalArgumentException("Baslik bulunamadi.");
        }

        String replyId = UUID.randomUUID().toString();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO forum_replies (id, topic_id, user_id, content)
                        VALUES (?, ?, ?, ?)
                        """)) {
            ps.setString(1, replyId);
            ps.setString(2, topicId);
            ps.setString(3, author.getId());
            ps.setString(4, body.trim());
            ps.executeUpdate();
            return findReplyById(replyId).orElseThrow(() -> new IllegalArgumentException("Yanit olusturulamadi."));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Yanit veritabanina kaydedilemedi.");
        }
    }

    public void deleteTopic(BaseUser actor, String topicId) {
        ForumTopic topic = findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        if (!canManageTopic(actor, topic)) {
            throw new IllegalArgumentException("Bu basligi silme yetkiniz yok.");
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement replies = conn.prepareStatement("DELETE FROM forum_replies WHERE topic_id = ?")) {
                replies.setString(1, topicId);
                replies.executeUpdate();
            }
            try (PreparedStatement topicDelete = conn.prepareStatement("DELETE FROM forum_topics WHERE id = ?")) {
                topicDelete.setString(1, topicId);
                topicDelete.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Baslik silinemedi.");
        }
    }

    public ForumReply editReply(BaseUser actor, String topicId, String replyId, String body) {
        validateText(body, "Yanit");
        ForumReply reply = findReplyById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Yanit bulunamadi."));
        if (!topicId.equals(reply.getTopicId())) {
            throw new IllegalArgumentException("Yanit bu basliga ait degil.");
        }
        if (!canManageReply(actor, reply)) {
            throw new IllegalArgumentException("Bu yaniti duzenleme yetkiniz yok.");
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        UPDATE forum_replies
                        SET content = ?
                        WHERE id = ?
                        """)) {
            ps.setString(1, body.trim());
            ps.setString(2, replyId);
            ps.executeUpdate();
            return findReplyById(replyId).orElse(reply);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Yanit guncellenemedi.");
        }
    }

    public void deleteReply(BaseUser actor, String topicId, String replyId) {
        ForumReply reply = findReplyById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Yanit bulunamadi."));
        if (!topicId.equals(reply.getTopicId())) {
            throw new IllegalArgumentException("Yanit bu basliga ait degil.");
        }
        if (!canManageReply(actor, reply)) {
            throw new IllegalArgumentException("Bu yaniti silme yetkiniz yok.");
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("DELETE FROM forum_replies WHERE id = ?")) {
            ps.setString(1, replyId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Yanit silinemedi.");
        }
    }

    public Optional<ForumTopic> findTopicById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        SELECT t.*, c.name AS category_name,
                               (SELECT COUNT(*) FROM forum_replies r WHERE r.topic_id = t.id) AS reply_count
                        FROM forum_topics t
                        JOIN forum_categories c ON c.id = t.category_id
                        WHERE t.id = ?
                        """)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildTopic(rs, true));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private List<ForumTopic> searchTopics(String categoryId, String query) {
        String q = query == null ? "" : query.trim();
        List<ForumTopic> topics = new ArrayList<>();
        String sql = """
                SELECT t.*, c.name AS category_name,
                       (SELECT COUNT(*) FROM forum_replies r WHERE r.topic_id = t.id) AS reply_count
                FROM forum_topics t
                JOIN forum_categories c ON c.id = t.category_id
                WHERE (? IS NULL OR t.category_id = ?)
                  AND (? = '' OR LOWER(t.title) LIKE ? OR LOWER(t.content) LIKE ?)
                ORDER BY t.is_pinned DESC, t.created_at DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            String categoryParam = categoryId == null || categoryId.isBlank() ? null : categoryId;
            String like = "%" + q.toLowerCase() + "%";
            ps.setString(1, categoryParam);
            ps.setString(2, categoryParam);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    topics.add(buildTopic(rs, false));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return topics;
    }

    private Optional<ForumReply> findReplyById(String replyId) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        SELECT r.*, u.username, u.password, u.role, u.profession
                        FROM forum_replies r
                        LEFT JOIN users u ON u.id = r.user_id
                        WHERE r.id = ?
                        """)) {
            ps.setString(1, replyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(buildReply(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private ForumTopic buildTopic(ResultSet rs, boolean includeReplies) throws Exception {
        BaseUser author = loadUserById(rs.getString("user_id"));
        ForumTopic topic = new ForumTopic(
                author,
                rs.getString("title"),
                rs.getString("content"),
                rs.getString("category_id"),
                rs.getString("category_name"));
        topic.setId(rs.getString("id"));
        topic.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        topic.setReplyCount(rs.getInt("reply_count"));
        topic.setViewCount(rs.getInt("view_count"));

        if (includeReplies) {
            for (ForumReply reply : getRepliesForTopic(topic.getId())) {
                topic.addReply(reply);
            }
            topic.setReplyCount(topic.getReplies().size());
        }
        return topic;
    }

    private List<ForumReply> getRepliesForTopic(String topicId) {
        List<ForumReply> replies = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("""
                        SELECT r.*, u.username, u.password, u.role, u.profession
                        FROM forum_replies r
                        LEFT JOIN users u ON u.id = r.user_id
                        WHERE r.topic_id = ?
                        ORDER BY r.created_at ASC
                        """)) {
            ps.setString(1, topicId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    replies.add(buildReply(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return replies;
    }

    private ForumReply buildReply(ResultSet rs) throws Exception {
        ForumReply reply = new ForumReply(
                loadUserById(rs.getString("user_id")),
                rs.getString("content"),
                rs.getString("topic_id"));
        reply.setId(rs.getString("id"));
        reply.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
        return reply;
    }

    private BaseUser loadUserById(String id) {
        if (id == null) {
            return null;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String role = rs.getString("role");
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        return new Admin(rs.getString("username"), rs.getString("id"), rs.getString("password"));
                    }
                    if ("COUNSELOR".equalsIgnoreCase(role)) {
                        return new Counselor(
                                rs.getString("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("profession"));
                    }
                    return new StandardUser(
                            rs.getString("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            UserRole.CLIENT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MainApp.userDatabase.values().stream()
                .filter(user -> id.equals(user.getId()))
                .findFirst()
                .orElse(null);
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime();
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " bos birakilamaz.");
        }
    }

    private boolean canManageReply(BaseUser actor, ForumReply reply) {
        if (actor == null || reply == null) {
            return false;
        }
        return reply.isAuthor(actor) || actor.getRole() == UserRole.ADMIN;
    }

    private boolean canManageTopic(BaseUser actor, ForumTopic topic) {
        if (actor == null || topic == null) {
            return false;
        }
        return topic.isAuthor(actor) || actor.getRole() == UserRole.ADMIN;
    }

    /** Hesap silinirken: kullanıcının tüm forum başlıkları ve yanıtları kaldırılır. */
    public void purgeAllContentForUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM forum_replies
                    WHERE topic_id IN (SELECT id FROM forum_topics WHERE user_id = ?)
                    """)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM forum_topics WHERE user_id = ?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM forum_replies WHERE user_id = ?")) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ForumTopic getLatestTopicForUser(BaseUser user) {
        if (user == null) return null;
        String sql = """
            SELECT topic_id FROM (
                SELECT id as topic_id, created_at FROM forum_topics WHERE user_id = ?
                UNION ALL
                SELECT topic_id, created_at FROM forum_replies WHERE user_id = ?
            ) AS combined
            ORDER BY created_at DESC
            LIMIT 1
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return findTopicById(rs.getString("topic_id")).orElse(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
