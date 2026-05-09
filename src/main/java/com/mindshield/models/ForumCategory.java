package com.mindshield.models;

public class ForumCategory {
    private final String id;
    private final String name;
    private final String description;
    private final int topicCount;

    public ForumCategory(String id, String name, String description, int topicCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.topicCount = topicCount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getTopicCount() {
        return topicCount;
    }
}
