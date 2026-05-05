package com.mindshield.models;

import java.time.LocalDateTime;

public class Report {
    private String id;
    private BaseUser reporter;
    private String contentId;
    private String reason;
    private LocalDateTime timestamp;

    public Report(String id, BaseUser reporter, String contentId, String reason) {
        this.id = id;
        this.reporter = reporter;
        this.contentId = contentId;
        this.reason = reason;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public BaseUser getReporter() { return reporter; }
    public String getContentId() { return contentId; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "Rapor eden: " + reporter.getPersona() + " | Sebep: " + reason;
    }
}
