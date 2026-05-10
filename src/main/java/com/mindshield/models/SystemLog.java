package com.mindshield.models;

import java.time.LocalDateTime;

public class SystemLog {
    private String id;
    private String action;
    private LocalDateTime timestamp;

    public SystemLog(String id, String action) {
        this.id = id;
        this.action = action;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getAction() { return action; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "[" + timestamp.toString().substring(0, 19) + "] " + action;
    }
}
