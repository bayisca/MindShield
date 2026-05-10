package com.mindshield.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChatMessage {

    private String id;
    private String roomId;
    private BaseUser sender;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage(String roomId, BaseUser sender, String content) {
        this.id = UUID.randomUUID().toString();
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public void setId(String id) { this.id = id; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getId()            { return id; }
    public String getRoomId()        { return roomId; }
    public BaseUser getSender()      { return sender; }
    public String getContent()       { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }
}