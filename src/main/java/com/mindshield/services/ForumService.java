package com.mindshield.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.ForumReply;
import com.mindshield.models.ForumTopic;
import com.mindshield.ui.UserRole;

public class ForumService {
    private final List<ForumTopic> topics = new ArrayList<>();

    public List<ForumTopic> getAllTopics() {
        return topics.stream()
                .sorted(Comparator.comparing(ForumTopic::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<ForumTopic> searchTopics(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            return getAllTopics();
        }
        return getAllTopics().stream()
                .filter(t -> t.getTitle().toLowerCase().contains(q)
                        || t.getBody().toLowerCase().contains(q)
                        || t.getAuthor().getPersona().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    public List<ForumTopic> getTopicsByAuthor(BaseUser author) {
        if (author == null) {
            return List.of();
        }
        return getAllTopics().stream()
                .filter(t -> t.isAuthor(author))
                .collect(Collectors.toList());
    }

    public ForumTopic createTopic(BaseUser author, String title, String body) {
        validateText(title, "Baslik");
        validateText(body, "Icerik");
        ForumTopic topic = new ForumTopic(author, title.trim(), body.trim());
        topics.add(0, topic);
        return topic;
    }

    public ForumReply addReply(BaseUser author, String topicId, String body) {
        validateText(body, "Yanit");
        ForumTopic topic = findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        ForumReply reply = new ForumReply(author, body.trim(), topicId);
        topic.addReply(reply);
        return reply;
    }

    public void deleteTopic(BaseUser actor, String topicId) {
        ForumTopic topic = findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        if (!canManageTopic(actor, topic)) {
            throw new IllegalArgumentException("Bu basligi silme yetkiniz yok.");
        }
        topics.removeIf(t -> t.getId().equals(topicId));
    }

    public ForumReply editReply(BaseUser actor, String topicId, String replyId, String body) {
        validateText(body, "Yanit");
        ForumTopic topic = findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        ForumReply reply = topic.findReplyById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Yanit bulunamadi."));
        if (!canManageReply(actor, reply)) {
            throw new IllegalArgumentException("Bu yaniti duzenleme yetkiniz yok.");
        }
        reply.setBody(body.trim());
        return reply;
    }

    public void deleteReply(BaseUser actor, String topicId, String replyId) {
        ForumTopic topic = findTopicById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Baslik bulunamadi."));
        ForumReply reply = topic.findReplyById(replyId)
                .orElseThrow(() -> new IllegalArgumentException("Yanit bulunamadi."));
        if (!canManageReply(actor, reply)) {
            throw new IllegalArgumentException("Bu yaniti silme yetkiniz yok.");
        }
        topic.deleteReplyById(replyId);
    }

    public Optional<ForumTopic> findTopicById(String id) {
        return topics.stream().filter(t -> t.getId().equals(id)).findFirst();
    }

    private void validateText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " bos birakilamaz.");
        }
    }

    private boolean canManageReply(BaseUser actor, ForumReply reply) {
        if (actor == null || reply == null) {
            return false;
        }
        return reply.isAuthor(actor) || actor.getRole() == UserRole.ADMIN;
    }

    private boolean canManageTopic(BaseUser actor, ForumTopic topic) {
        if (actor == null || topic == null) {
            return false;
        }
        return topic.isAuthor(actor) || actor.getRole() == UserRole.ADMIN;
    }
}
