package com.mindshield.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class ModerationReport implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Kind implements Serializable {
        BLOG_POST,
        ROOM_MESSAGE,
        DIRECT_USER
    }

    private String id;
    private Kind kind;
    private String reporterPersona;
    /** Şikayet edilen kullanıcının persona / görünen adı. */
    private String reportedPersona;
    private String postId;
    private String roomId;
    private String messageId;
    private String messagePreview;
    private String reason;
    private LocalDateTime createdAt;
    private boolean resolved;

    public ModerationReport(Kind kind,
                            String reporterPersona,
                            String reportedPersona,
                            String postId,
                            String roomId,
                            String messageId,
                            String messagePreview,
                            String reason) {
        this.id = UUID.randomUUID().toString();
        this.kind = kind;
        this.reporterPersona = reporterPersona != null ? reporterPersona : "";
        this.reportedPersona = reportedPersona != null ? reportedPersona : "";
        this.postId = postId;
        this.roomId = roomId;
        this.messageId = messageId;
        this.messagePreview = messagePreview != null ? messagePreview : "";
        this.reason = reason != null ? reason : "";
        this.createdAt = LocalDateTime.now();
        this.resolved = false;
    }

    public String getId() {
        return id;
    }

    public Kind getKind() {
        return kind;
    }

    public String getReporterPersona() {
        return reporterPersona;
    }

    public String getReportedPersona() {
        return reportedPersona;
    }

    public String getPostId() {
        return postId;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessagePreview() {
        return messagePreview;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    @Override
    public String toString() {
        String head = switch (kind) {
            case BLOG_POST -> "[Blog] ";
            case ROOM_MESSAGE -> "[Grup] ";
            case DIRECT_USER -> "[DM] ";
        };
        return head + "@" + reportedPersona + " — " + (reason.length() > 48 ? reason.substring(0, 45) + "…" : reason);
    }
}
