package com.mindshield.services;

import com.mindshield.models.BaseUser;
import com.mindshield.models.BlogPost;
import com.mindshield.models.Comment;
import com.mindshield.models.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PostService {
    private List<BlogPost> blogPosts;
    private List<Comment> comments;

    public PostService() {
        this.blogPosts = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    public BlogPost createPost(BaseUser author, String title, String body) {
        BlogPost post = new BlogPost(author, title, body);
        blogPosts.add(post);
        return post;
    }

    public BlogPost publishPost(String postId) {
        BlogPost post = findPostById(postId);
        if (post != null) {
            post.publish();
        }
        return post;
    }

    public Comment addComment(String postId, BaseUser author, String body) {
        BlogPost post = findPostById(postId);
        if (post != null) {
            Comment comment = new Comment(author, body, postId);
            comments.add(comment);
            post.addComment(comment);
            return comment;
        }
        return null;
    }

    public List<BlogPost> searchPosts(String searchTerm) {
        return blogPosts.stream()
                .filter(post -> post.containsInTitle(searchTerm) || post.containsInBody(searchTerm))
                .collect(Collectors.toList());
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

    public BlogPost findPostById(String postId) {
        return blogPosts.stream()
                .filter(post -> post.getId().equals(postId))
                .findFirst()
                .orElse(null);
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
                .filter(post -> post.getAuthor().equals(author))
                .collect(Collectors.toList());
    }
}
