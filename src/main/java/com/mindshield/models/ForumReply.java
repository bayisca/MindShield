package com.mindshield.models;

public class ForumReply extends Content {
    private final String topicId;

    public ForumReply(BaseUser author, String body, String topicId) {
        super(author, "", body);
        this.topicId = topicId;
    }

    public String getTopicId() {
        return topicId;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void setTitle(String title) {
        // Replies do not have titles.
    }
}
