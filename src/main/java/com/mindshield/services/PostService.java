package com.mindshield.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.mindshield.dao.DatabaseConnection;
import com.mindshield.exceptions.PostNotFoundException;
import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import com.mindshield.models.Counselor;
import com.mindshield.models.StandardUser;
import com.mindshield.ui.UserRole;

public class PostService {

    private static final int MAX_POST_WORD_LIMIT = 5000;

    // WORD LIMIT--------------
    
    public void validateWordLimit(String body) {

        if (body == null || body.trim().isEmpty())
            return;

        String[] words = body.trim().split("\\s+");

        if (words.length > MAX_POST_WORD_LIMIT) {
            throw new IllegalArgumentException("Post body exceeds limit.");
        }
    }

    public int getWordCount(String text) {

        if (text == null || text.trim().isEmpty())
            return 0;

        return text.trim().split("\\s+").length;
    }

    public int getMaxWordLimit() {
        return MAX_POST_WORD_LIMIT;
    }

    // CREATE POST------------

    public BlogPost createPost(BaseUser author, String title, String body) {

        if (author.getRole() != UserRole.COUNSELOR) {
            throw new UnauthorizedException(
                    "Only counselors can create posts.");
        }

        validateWordLimit(body);

        String postId = java.util.UUID.randomUUID().toString();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO blog_posts (
                        id,
                        user_id,
                        title,
                        content
                    )
                    VALUES (?, ?, ?, ?)
                    """);

            ps.setString(1, postId);
            ps.setString(2, author.getId());
            ps.setString(3, title);
            ps.setString(4, body);

            ps.executeUpdate();

            return findPostById(postId);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Create post failed");
        }
    }

   
    // UPDATE POST---------------------
  
    public BlogPost updatePost(
            BaseUser author,
            String postId,
            String title,
            String body) {

        validateWordLimit(body);

        BlogPost post = findPostById(postId);

        if (!post.isAuthor(author)
                && author.getRole() != UserRole.ADMIN) {

            throw new UnauthorizedException("Not allowed.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    UPDATE blog_posts
                    SET title = ?, content = ?
                    WHERE id = ?
                    """);

            ps.setString(1, title);
            ps.setString(2, body);
            ps.setString(3, postId);

            ps.executeUpdate();

            return findPostById(postId);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Update failed");
        }
    }

    // DELETE POST---------------------------------  

    public BlogPost unpublishPost(BaseUser author, String postId) {

        BlogPost post = findPostById(postId);

        if (!post.isAuthor(author)
                && author.getRole() != UserRole.ADMIN) {

            throw new UnauthorizedException("Not allowed.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM blog_posts
                    WHERE id = ?
                    """);

            ps.setString(1, postId);

            ps.executeUpdate();

            return post;

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Delete failed");
        }
    }

    // ADMIN DELETE---------------------------------

    public BlogPost deletePostAsAdmin(
            BaseUser admin,
            String postId) {

        if (admin == null
                || admin.getRole() != UserRole.ADMIN) {

            throw new UnauthorizedException("Only admin.");
        }

        return unpublishPost(admin, postId);
    }

   
    // ADD COMMENT------------------------------  

    public Comment addComment(
            String postId,
            BaseUser author,
            String body) {

        String commentId =
                java.util.UUID.randomUUID().toString();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    INSERT INTO blog_comments (
                        id,
                        post_id,
                        user_id,
                        content
                    )
                    VALUES (?, ?, ?, ?)
                    """);

            ps.setString(1, commentId);
            ps.setString(2, postId);
            ps.setString(3, author.getId());
            ps.setString(4, body);

            ps.executeUpdate();

            return new Comment(author, body, postId);

        } catch (Exception e) {

            e.printStackTrace();
            throw new RuntimeException("Comment add failed");
        }
    }

    // DELETE COMMENT-----------------------------------------

    public void deleteComment(
            String commentId,
            BaseUser actor) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement find = conn.prepareStatement("""
                    SELECT * FROM blog_comments
                    WHERE id = ?
                    """);

            find.setString(1, commentId);

            ResultSet rs = find.executeQuery();

            if (!rs.next()) {
                throw new PostNotFoundException(
                        "Comment not found");
            }

            String ownerId = rs.getString("user_id");

            boolean ownComment =
                    ownerId.equals(actor.getId());

            if (!ownComment
                    && actor.getRole() != UserRole.ADMIN) {

                throw new UnauthorizedException(
                        "Not allowed.");
            }

            PreparedStatement delete = conn.prepareStatement("""
                    DELETE FROM blog_comments
                    WHERE id = ?
                    """);

            delete.setString(1, commentId);

            delete.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // GET COMMENTS OF POST-----------------------------------------

    public List<Comment> getCommentsForPost(String postId) {

        List<Comment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM blog_comments
                    WHERE post_id = ?
                    ORDER BY created_at DESC
                    """);

            ps.setString(1, postId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                BaseUser author =
                        loadUserById(
                                rs.getString("user_id"));

                Comment comment = new Comment(
                        author,
                        rs.getString("content"),
                        postId);

                comments.add(comment);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return comments;
    }

    // SEARCH POSTS----------------------------------------

    public List<BlogPost> searchPosts(String searchTerm) {

        List<BlogPost> posts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM blog_posts
                    WHERE title LIKE ?
                    OR content LIKE ?
                    ORDER BY created_at DESC
                    """);

            ps.setString(1, "%" + searchTerm + "%");
            ps.setString(2, "%" + searchTerm + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                posts.add(buildPostFromResult(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return posts;
    }

    // GET ALL POSTS-------------------------------------------

    public List<BlogPost> getAllPosts() {  

        List<BlogPost> posts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM blog_posts
                    ORDER BY created_at DESC
                    """);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                posts.add(buildPostFromResult(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return posts;
    }

    // FIND POST-------------------------------------------

    public BlogPost findPostById(String postId) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM blog_posts
                    WHERE id = ?
                    """);

            ps.setString(1, postId);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {

                throw new PostNotFoundException(
                        "Post not found");
            }

            return buildPostFromResult(rs);

        } catch (Exception e) {

            throw new PostNotFoundException(
                    "Post not found");
        }
    }

    // POSTS BY AUTHOR-------------------------------------------

    public List<BlogPost> getPostsByAuthor(BaseUser author) {

        List<BlogPost> posts = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM blog_posts
                    WHERE user_id = ?
                    ORDER BY created_at DESC
                    """);

            ps.setString(1, author.getId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                posts.add(buildPostFromResult(rs));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return posts;
    }

    // --------------------------------------------------
    // DELETE ALL POSTS OF USER
    // --------------------------------------------------

    public void deleteAllPostsFor(BaseUser author) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    DELETE FROM blog_posts
                    WHERE user_id = ?
                    """);

            ps.setString(1, author.getId());

            ps.executeUpdate();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /** Başkalarının yazılarına yapılan yorumları da kaldırır (hesap silme). */
    public void deleteAllCommentsForUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM blog_comments WHERE user_id = ?")) {
            ps.setString(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // LOAD USER----------------------------------------

    private BaseUser loadUserById(String id) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            PreparedStatement ps = conn.prepareStatement("""
                    SELECT * FROM users
                    WHERE id = ?
                    """);

            ps.setString(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next())
                return null;

            String role = rs.getString("role");

            if ("COUNSELOR".equals(role)) {

                return new Counselor(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("profession"));

            } else {

                return new StandardUser(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        UserRole.CLIENT);
            }

        } catch (Exception e) {

            e.printStackTrace();
            return null;
        }
    }

    // BUILD POST------------------------------------

    private BlogPost buildPostFromResult(ResultSet rs) throws Exception {

    String postId = rs.getString("id");

    BaseUser author = loadUserById(rs.getString("user_id"));

    BlogPost post = new BlogPost(
            postId,
            author,
            rs.getString("title"),
            rs.getString("content")
    );

    List<Comment> comments = getCommentsForPost(postId);

    for (Comment c : comments) {
        post.addComment(c);
    }

    return post;
}
}