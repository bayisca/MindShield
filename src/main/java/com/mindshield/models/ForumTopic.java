package com.mindshield.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ForumTopic extends Content {
    private final List<ForumReply> replies = new ArrayList<>();

    public ForumTopic(BaseUser author, String title, String body) {
        super(author, title, body);
    }

    public void addReply(ForumReply reply) {
        if (reply != null) {
            replies.add(reply);
            touchUpdated();
        }
    }

    public List<ForumReply> getReplies() {
        return new ArrayList<>(replies);
    }

    public Optional<ForumReply> findReplyById(String replyId) {
        if (replyId == null) return Optional.empty();
        return replies.stream().filter(r -> replyId.equals(r.getId())).findFirst();
    }

    public boolean deleteReplyById(String replyId) {
        if (replyId == null) return false;
        boolean removed = replies.removeIf(r -> replyId.equals(r.getId()));
        if (removed) {
            touchUpdated();
        }
        return removed;
    }
}
