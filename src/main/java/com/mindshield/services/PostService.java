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
import com.mindshield.models.Content;
import com.mindshield.ui.UserRole;


//Service class for managing blog posts and comments.

public class PostService {
    private List<BlogPost> blogPosts;
    private List<Comment> comments;
    private PostDao postDao;
    private static final int MAX_POST_WORD_LIMIT = 5000; // Maximum word limit for posts

    public PostService() {
        this.postDao = new PostDaoImpl();
        this.blogPosts = postDao.findAll();
        this.comments = new ArrayList<>();
    }

    /**
     * Seeds sample blog posts on first run (when the post list is empty).
     * Only called once from MainApp's static initialiser.
     */
    public void seedSamplePosts(BaseUser admin) {
        if (!blogPosts.isEmpty()) return; // Already seeded from persisted data

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

    
    //Validates the word count of a post body.

    public void validateWordLimit(String body) {
        if (body == null || body.trim().isEmpty()) {
            return; // Empty body is handled elsewhere
        }
        
        String[] words = body.trim().split("\\s+");
        int wordCount = words.length;
        
        if (wordCount > MAX_POST_WORD_LIMIT) {
            throw new IllegalArgumentException(
                "Post body exceeds the maximum word limit of " + MAX_POST_WORD_LIMIT + 
                " words. Current word count: " + wordCount);
        }
    }

    
    //Gets the current word count of a given text.
    
    public int getWordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }


    //Gets the maximum word limit for posts.
    
    public int getMaxWordLimit() {
        return MAX_POST_WORD_LIMIT;
    }

    //Creates a new blog post. Only counselors are authorized to create posts.
    public BlogPost createPost(BaseUser author, String title, String body) {
        if (author.getRole() != UserRole.COUNSELOR) {
            throw new UnauthorizedException("Only counselors can create blog posts.");
        }
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title and body cannot be empty.");
        }
        
        validateWordLimit(body); // Validate word limit before creating post

        BlogPost post = new BlogPost(author, title, body);
        blogPosts.add(post);
        postDao.save(post); // Save to DAO
        return post;
    }

    // Publishes a blog post by its ID.
    public BlogPost publishPost(String postId) {
        if (postId == null || postId.trim().isEmpty()) {
            throw new IllegalArgumentException("Post ID cannot be null or empty.");
        }
        
        try {
            BlogPost post = findPostById(postId);
            
            if (post.isPublished()) {
                throw new IllegalStateException("Post is already published.");
            }
            
            post.publish();
            postDao.update(); // Save changes to persistent storage
            return post;
        } catch (PostNotFoundException e) {
            throw e; // Rethrow specific exception for not found
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish post: " + e.getMessage(), e); 
        }
    }

    public BlogPost updatePost(BaseUser author, String postId, String title, String body) {
        if (author == null) {
            throw new UnauthorizedException("Authentication required to update a post.");
        }
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title and body cannot be empty.");
        }
        
        validateWordLimit(body); 

        BlogPost post = findPostById(postId);
        if (!post.isAuthor(author) && author.getRole() != UserRole.SUPERADMIN) {
            throw new UnauthorizedException("You can only update your own posts.");
        }

        post.setTitle(title);
        post.setBody(body);
        postDao.update(); 
        return post;
    }

    public BlogPost unpublishPost(BaseUser author, String postId) {
        if (author == null) {
            throw new UnauthorizedException("Authentication required to delete a post.");
        }

        BlogPost post = findPostById(postId); 
        if (!post.isAuthor(author) && author.getRole() != UserRole.SUPERADMIN) {
            throw new UnauthorizedException("You can only delete your own posts.");
        }

        blogPosts.removeIf(p -> p != null && postId.equals(p.getId())); // Remove post from in-memory list
        comments.removeIf(c -> c != null && postId.equals(c.getParentId())); // Remove associated comments
        postDao.deleteById(postId); // Remove post from persistent storage
        return post;
    }

    
    //Adds a comment to an existing blog post.
    public Comment addComment(String postId, BaseUser author, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment body cannot be empty.");
        }
        
        BlogPost post = findPostById(postId);
        Comment comment = new Comment(author, body, postId);
        comments.add(comment);
        post.addComment(comment);
        postDao.update(); // Save changes to persistent storage
        
        return comment;
    }

    public Comment updateComment(BaseUser author, String postId, String commentId, String body) {
        if (author == null) {
            throw new UnauthorizedException("Authentication required to update a comment.");
        }
        if (body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment body cannot be empty.");
        }

        BlogPost post = findPostById(postId);
        Comment comment = post.findCommentById(commentId);
        
        if (comment == null) {
            throw new PostNotFoundException("Comment not found with ID: " + commentId);
        }

        boolean canEdit = comment.isAuthor(author) || post.isAuthor(author) || author.getRole() == UserRole.SUPERADMIN;
        if (!canEdit) {
            throw new UnauthorizedException("You are not allowed to edit this comment.");
        }

        comment.setBody(body);
        postDao.update(); 
        return comment;
    }

    public Comment deleteComment(BaseUser author, String postId, String commentId) {
        if (author == null) {
            throw new UnauthorizedException("Authentication required to delete a comment.");
        }

        BlogPost post = findPostById(postId);
        Comment existing = post.findCommentById(commentId);
        
        if (existing == null) {
            throw new PostNotFoundException("Comment not found with ID: " + commentId);
        }

        boolean canDelete = existing.isAuthor(author) || post.isAuthor(author) || author.getRole() == UserRole.SUPERADMIN; //
        if (!canDelete) {
            throw new UnauthorizedException("You are not allowed to delete this comment.");
        }

        Comment removed = post.removeCommentById(commentId);
        comments.removeIf(c -> c != null && commentId.equals(c.getId()));
        postDao.update();
        return removed;
    }

    
    // Searches posts by keyword in title or body via DAO.
    public List<BlogPost> searchPosts(String searchTerm) {
        // Connected to DAO structure as requested
        return postDao.searchByTitleOrContent(searchTerm);
    }

    public List<BlogPost> searchPostsByTitle(String title) {
        return blogPosts.stream()
                .filter(post -> post.containsInTitle(title))
                .collect(Collectors.toList());
    }

    public List<BlogPost> searchPostsByContent(String content) {
        return blogPosts.stream()
                .filter(post -> post.containsInBody(content))
                .collect(Collectors.toList());
    }

    public List<BlogPost> getPublishedPosts() {
        return blogPosts.stream()
                .filter(BlogPost::isPublished)
                .collect(Collectors.toList());
    }

    public List<BlogPost> getAllPosts() {
        return new ArrayList<>(blogPosts);
    }

    
    // Finds a post by its ID.
    
    public BlogPost findPostById(String postId) {
        return blogPosts.stream()
                .filter(post -> post.getId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new PostNotFoundException("Blog post not found with ID: " + postId)); 
    }

    public List<Comment> getCommentsForPost(String postId) {
        return comments.stream()
                .filter(comment -> postId.equals(comment.getParentId()))
                .collect(Collectors.toList());
    }

    public String getPersonaNameForContent(Content content) {
        return content.getPersonaName();
    }

    public List<BlogPost> getPostsByAuthor(BaseUser author) {
        return blogPosts.stream()
                .filter(post -> post.isAuthor(author))
                .collect(Collectors.toList());
    }
}
