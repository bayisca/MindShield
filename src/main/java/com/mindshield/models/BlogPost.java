package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;

public class BlogPost extends Content {

    private String id;
    private List<Comment> comments;
    private boolean isPublished;

    public BlogPost(String id, BaseUser author, String title, String body) {
        super(author, title, body);
        this.id = id;
        this.comments = new ArrayList<>();
        this.isPublished = false;
    }

    public String getId() {
        return id;
    }

    public List<Comment> getComments() {
        return new ArrayList<>(comments);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void publish() {
        this.isPublished = true;
    }

    public void unpublish() {
        this.isPublished = false;
    }
}