package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDaoImpl implements PostDao {
    private final UserDao userDao;

    public PostDaoImpl() {
        this.userDao = new UserDaoImpl();
    }

    @Override
    public void save(BlogPost post) {
        String sql = "MERGE INTO blog_posts (id, user_id, title, content, created_at) KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, post.getId());
            ps.setString(2, post.getAuthor().getId());
            ps.setString(3, post.getTitle());
            ps.setString(4, post.getBody());
            ps.setTimestamp(5, Timestamp.valueOf(post.getCreatedAt()));
            ps.executeUpdate();
            saveComments(post);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveComments(BlogPost post) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Merging comments
            String sql = "MERGE INTO blog_comments (id, post_id, user_id, content, created_at) KEY(id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Comment c : post.getComments()) {
                    ps.setString(1, c.getId());
                    ps.setString(2, post.getId());
                    ps.setString(3, c.getAuthor().getId());
                    ps.setString(4, c.getBody());
                    ps.setTimestamp(5, Timestamp.valueOf(c.getCreatedAt()));
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM blog_posts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BlogPost findById(String id) {
        String sql = "SELECT * FROM blog_posts WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPost(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<BlogPost> findAll() {
        List<BlogPost> posts = new ArrayList<>();
        String sql = "SELECT * FROM blog_posts ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                posts.add(mapPost(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public List<BlogPost> searchByTitleOrContent(String searchTerm) {
        List<BlogPost> posts = new ArrayList<>();
        String sql = "SELECT * FROM blog_posts WHERE title LIKE ? OR content LIKE ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String term = "%" + searchTerm + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapPost(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private BlogPost mapPost(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        BaseUser author = userDao.findById(rs.getString("user_id"));
        BlogPost post = new BlogPost(id, author, rs.getString("title"), rs.getString("content"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // Load comments
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM blog_comments WHERE post_id = ? ORDER BY created_at ASC")) {
            ps.setString(1, id);
            try (ResultSet crs = ps.executeQuery()) {
                while (crs.next()) {
                    BaseUser cAuthor = userDao.findById(crs.getString("user_id"));
                    Comment comment = new Comment(cAuthor, crs.getString("content"));
                    comment.setId(crs.getString("id"));
                    comment.setCreatedAt(crs.getTimestamp("created_at").toLocalDateTime());
                    post.addComment(comment);
                }
            }
        }
        return post;
    }
    @Override
    public void update() {
        // Satisfaction of interface. Specific updates are handled by save (MERGE) or direct SQL in services.
    }

    // ── Favori Blog İşlemleri ──────────────────────────────────

    @Override
    public boolean isFavoriteBlog(String userId, String postId) {
        String sql = "SELECT 1 FROM favorite_blogs WHERE user_id = ? AND post_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, postId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void addFavoriteBlog(String userId, String postId) {
        if (isFavoriteBlog(userId, postId)) return;
        String sql = "INSERT INTO favorite_blogs (id, user_id, post_id) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, java.util.UUID.randomUUID().toString());
            ps.setString(2, userId);
            ps.setString(3, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeFavoriteBlog(String userId, String postId) {
        String sql = "DELETE FROM favorite_blogs WHERE user_id = ? AND post_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<BlogPost> getFavoriteBlogs(String userId) {
        List<BlogPost> posts = new ArrayList<>();
        String sql = "SELECT b.* FROM blog_posts b JOIN favorite_blogs f ON b.id = f.post_id WHERE f.user_id = ? ORDER BY f.added_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapPost(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public void deleteAllFavoriteBlogsForUser(String userId) {
        String sql = "DELETE FROM favorite_blogs WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
