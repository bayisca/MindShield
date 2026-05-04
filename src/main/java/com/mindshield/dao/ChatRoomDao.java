package com.mindshield.dao;

import com.mindshield.models.ChatRoom;
import java.util.List;

public interface ChatRoomDao {
    void save(ChatRoom room);
    void update(ChatRoom room);
    void deleteById(String id);
    ChatRoom findById(String id);
    List<ChatRoom> findAll();
}