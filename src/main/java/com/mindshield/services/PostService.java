package com.mindshield.services;

import com.mindshield.exceptions.PostNotFoundException;
import com.mindshield.exceptions.UnauthorizedException;
import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import com.mindshield.models.Content;
import com.mindshield.ui.UserRole;
import com.mindshield.dao.PostDao;
import com.mindshield.dao.PostDaoImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing blog posts and comments.
 * Handles business logic, RBAC, and data validation.
 */
public class PostService {
    private List<BlogPost> blogPosts;
    private List<Comment> comments;
    private PostDao postDao;

    public PostService() {
        this.postDao = new PostDaoImpl(); // Connected to Ahmet's DAO structure
        this.blogPosts = postDao.findAll(); // Load existing posts from DAO
        this.comments = new ArrayList<>();
    }

    /**
     * Creates a new blog post. Only counselors are authorized to create posts.
     * 
     * @param author The user creating the post
     * @param title The title of the post
     * @param body The content of the post
     * @return The created BlogPost
     * @throws UnauthorizedException if the author is not a COUNSELOR
     * @throws IllegalArgumentException if title or body is empty
     */
    public BlogPost createPost(BaseUser author, String title, String body) {
        if (author.getRole() != UserRole.COUNSELOR) {
            throw new UnauthorizedException("Only counselors can create blog posts.");
        }
        if (title == null || title.trim().isEmpty() || body == null || body.trim().isEmpty()) {
            throw new IllegalArgumentException("Post title and body cannot be empty.");
        }

        BlogPost post = new BlogPost(author, title, body);
        blogPosts.add(post);
        postDao.save(post); // Save to DAO
        return post;
    }

    /**
     * Publishes a blog post by its ID.
     * 
     * @param postId The ID of the post to publish
     * @return The published BlogPost
     * @throws PostNotFoundException if the post does not exist
     */
    public BlogPost publishPost(String postId) {
        BlogPost post = findPostById(postId);
        post.publish();
        postDao.update(); // Save changes to persistent storage
        return post;
    }

    /**
     * Adds a comment to an existing blog post.
     * 
     * @param postId The ID of the post
     * @param author The user creating the comment
     * @param body The content of the comment
     * @return The created Comment
     * @throws PostNotFoundException if the post does not exist
     * @throws IllegalArgumentException if the comment body is empty
     */
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

    /**
     * Searches posts by keyword in title or body via DAO.
     * 
     * @param searchTerm The keyword to search for
     * @return A list of matching blog posts
     */
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

    /**
     * Finds a post by its ID.
     * 
     * @param postId The ID to look for
     * @return The matching BlogPost
     * @throws PostNotFoundException if no post is found
     */
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
