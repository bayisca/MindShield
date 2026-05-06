package com.mindshield.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.dao.PostDao;
import com.mindshield.dao.PostDaoImpl;
import com.mindshield.exceptions.PostNotFoundException;
import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import com.mindshield.ui.UserRole;

/**
 * Service class for managing blog posts and comments.
 */
public class PostService {
    private List<BlogPost> blogPosts;
    private List<Comment> comments;
    private PostDao postDao;
    private static final int MAX_POST_WORD_LIMIT = 5000;

    public PostService() {
        this.postDao = new PostDaoImpl();
        this.blogPosts = postDao.findAll();
        this.comments = new ArrayList<>();
    }

    /**
     * Seeds sample blog posts on first run.
     */
    public void seedSamplePosts(BaseUser admin) {
        if (!blogPosts.isEmpty()) return;

        BlogPost p1 = new BlogPost(admin, "Stresle Baş Etme Yolları",
                "Modern yaşamın getirdiği stresi yönetmek için bilimsel yöntemler:\n\n" +
                "1. Derin nefes egzersizleri günde 10 dakika yapın.\n" +
                "2. Düzenli yürüyüş ve hafif fiziksel aktivite kortizol seviyelerini düşürür.\n" +
                "3. Uyku düzeninizi koruyun — yetersiz uyku stresi katlar.\n" +
                "4. Sosyal destek sistemleri kurun; yalnız olmadığınızı hissetmek önemlidir.");
        blogPosts.add(p1);
        postDao.save(p1);

        BlogPost p2 = new BlogPost(admin, "Anksiyete ve Sosyal Fobi",
                "Sosyal ortamlarda rahat hissetmek için bilişsel davranışçı teknikler:\n\n" +
                "Sosyal fobi, yaşanan kaygı bozukluklarının en yaygın formlarından biridir. " +
                "Tetikleyici durumları tanıyın, negatif otomatik düşünceleri sorgulayın ve " +
                "kademeli maruz kalma egzersizleri yapın. Profesyonel destek almaktan çekinmeyin.");
        blogPosts.add(p2);
        postDao.save(p2);
    }

    public void validateWordLimit(String body) {
        if (body == null || body.trim().isEmpty()) {
            return;
        }
        String[] words = body.trim().split("\\s+");
        if (words.length > MAX_POST_WORD_LIMIT) {
            throw new IllegalArgumentException("Post body exceeds the maximum word limit.");
        }
    }

    public int getWordCount(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }

    public int getMaxWordLimit() {
        return MAX_POST_WORD_LIMIT;
    }

    public BlogPost createPost(BaseUser author, String title, String body) {
        if (author.getRole() != UserRole.COUNSELOR && author.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Only counselors can create blog posts.");
        }
        validateWordLimit(body);
        BlogPost post = new BlogPost(author, title, body);
        blogPosts.add(post);
        postDao.save(post);
        return post;
    }

    public BlogPost publishPost(String postId) {
        BlogPost post = findPostById(postId);
        if (post.isPublished()) {
            throw new IllegalStateException("Post is already published.");
        }
        post.publish();
        postDao.update();
        return post;
    }

    public BlogPost updatePost(BaseUser author, String postId, String title, String body) {
        validateWordLimit(body);
        BlogPost post = findPostById(postId);
        if (!post.isAuthor(author) && author.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You can only update your own posts.");
        }
        post.setTitle(title);
        post.setBody(body);
        postDao.update();
        return post;
    }

    public BlogPost unpublishPost(BaseUser author, String postId) {
        BlogPost post = findPostById(postId);
        if (!post.isAuthor(author) && author.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("You can only delete your own posts.");
        }
        blogPosts.removeIf(p -> p != null && postId.equals(p.getId()));
        comments.removeIf(c -> c != null && postId.equals(c.getParentId()));
        postDao.deleteById(postId);
        return post;
    }

    public BlogPost deletePostAsAdmin(BaseUser admin, String postId) {
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Bu işlem yalnızca yöneticiler içindir.");
        }
        BlogPost post = findPostById(postId);
        blogPosts.removeIf(p -> p != null && postId.equals(p.getId()));
        comments.removeIf(c -> c != null && postId.equals(c.getParentId()));
        postDao.deleteById(postId);
        return post;
    }

    public Comment addComment(String postId, BaseUser author, String body) {
        BlogPost post = findPostById(postId);
        Comment comment = new Comment(author, body, postId);
        comments.add(comment);
        post.addComment(comment);
        postDao.update();
        return comment;
    }

    public Comment updateComment(BaseUser author, String postId, String commentId, String body) {
        BlogPost post = findPostById(postId);
        Comment comment = post.findCommentById(commentId);
        if (comment == null) throw new PostNotFoundException("Comment not found.");
        if (!comment.isAuthor(author) && !post.isAuthor(author) && author.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Not allowed.");
        }
        comment.setBody(body);
        postDao.update();
        return comment;
    }

    public Comment deleteComment(BaseUser author, String postId, String commentId) {
        BlogPost post = findPostById(postId);
        Comment existing = post.findCommentById(commentId);
        if (existing == null) throw new PostNotFoundException("Comment not found.");
        if (!existing.isAuthor(author) && !post.isAuthor(author) && author.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedException("Not allowed.");
        }
        Comment removed = post.removeCommentById(commentId);
        comments.removeIf(c -> c != null && commentId.equals(c.getId()));
        postDao.update();
        return removed;
    }

    public List<BlogPost> searchPosts(String searchTerm) {
        return postDao.searchByTitleOrContent(searchTerm);
    }

    public List<BlogPost> getPublishedPosts() {
        return blogPosts.stream().filter(BlogPost::isPublished).collect(Collectors.toList());
    }

    public List<BlogPost> getAllPosts() {
        return new ArrayList<>(blogPosts);
    }

    public BlogPost findPostById(String postId) {
        return blogPosts.stream()
                .filter(post -> post.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));
    }

    public List<Comment> getCommentsForPost(String postId) {
        return comments.stream()
                .filter(comment -> postId.equals(comment.getParentId()))
                .collect(Collectors.toList());
    }

    public List<BlogPost> getPostsByAuthor(BaseUser author) {
        return blogPosts.stream()
                .filter(post -> post.isAuthor(author))
                .collect(Collectors.toList());
    }

    /**
     * Deletes all posts created by a specific user.
     * Useful for account cleanup.
     */
    public void deleteAllPostsFor(BaseUser author) {
        if (author == null) return;
        List<String> ids = blogPosts.stream()
                .filter(p -> p != null && p.isAuthor(author))
                .map(BlogPost::getId)
                .collect(Collectors.toList());
        for (String id : ids) {
            unpublishPost(author, id);
        }
    }
}
