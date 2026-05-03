package com.mindshield.models;


public class Comment extends Content {
    private String parentId;

    public Comment(BaseUser author, String body, String parentId) {
        super(author, "", body);
        this.parentId = parentId;
    }

    public Comment(BaseUser author, String body) {
        this(author, body, null);
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void setTitle(String title) {
        // Comments don't have titles
    }
}
