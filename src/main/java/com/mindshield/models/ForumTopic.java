package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForumTopic extends Content {
    private final List<ForumReply> replies = new ArrayList<>();
    private String categoryId;
    private String categoryName;
    private int replyCount;
    private int viewCount;

    public ForumTopic(BaseUser author, String title, String body) {
        super(author, title, body);
    }

    public ForumTopic(BaseUser author, String title, String body, String categoryId, String categoryName) {
        super(author, title, body);
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public void addReply(ForumReply reply) {
        if (reply != null) {
            replies.add(reply);
            replyCount = replies.size();
            touchUpdated();
        }
    }

    public List<ForumReply> getReplies() {
        return new ArrayList<>(replies);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getReplyCount() {
        return Math.max(replyCount, replies.size());
    }

    public void setReplyCount(int replyCount) {
        this.replyCount = replyCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Optional<ForumReply> findReplyById(String replyId) {
        if (replyId == null) return Optional.empty();
        return replies.stream().filter(r -> replyId.equals(r.getId())).findFirst();
    }

    public boolean deleteReplyById(String replyId) {
        if (replyId == null) return false;
        boolean removed = replies.removeIf(r -> replyId.equals(r.getId()));
        if (removed) {
            replyCount = replies.size();
            touchUpdated();
        }
        return removed;
    }
}
