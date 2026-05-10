package com.mindshield.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {
    private String id;
    private BaseUser sender;
    private BaseUser receiver;
    private String content;
    private LocalDateTime timestamp;

    public Message(BaseUser sender, BaseUser receiver, String content) {
        this.id = UUID.randomUUID().toString();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public BaseUser getSender() { return sender; }
    public BaseUser getReceiver() { return receiver; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
