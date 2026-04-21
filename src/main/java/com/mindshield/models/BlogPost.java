package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;

public class BlogPost extends Content {
    private List<Comment> comments;
    private boolean isPublished;

    public BlogPost(BaseUser author, String title, String body) {
        super(author, title, body);
        this.comments = new ArrayList<>();
        this.isPublished = false;
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

    public int getCommentCount() {
        return comments.size();
    }
}
