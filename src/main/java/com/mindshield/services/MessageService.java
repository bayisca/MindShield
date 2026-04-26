package com.mindshield.services;

import com.mindshield.models.BaseUser;
import com.mindshield.models.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MessageService handles all direct messaging logic between clients and counselors.
 * Features persistence to save messages across application restarts.
 */
public class MessageService {
    private List<Message> messages;
    private static final String DATA_FILE = "messages.dat";

    public MessageService() {
        this.messages = loadMessages();
    }

    /**
     * Sends a direct message from one user to another.
     * 
     * @param sender The user sending the message
     * @param receiver The user receiving the message
     * @param content The text content of the message
     * @return The created Message object
     * @throws IllegalArgumentException if the content is null or empty
     */
    public Message sendMessage(BaseUser sender, BaseUser receiver, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        Message message = new Message(sender, receiver, content);
        messages.add(message);
        saveMessages();
        return message;
    }

    /**
     * Retrieves all messages exchanged between two specific users.
     * 
     * @param user1 The first user
     * @param user2 The second user
     * @return A list of messages between the two users
     */
    public List<Message> getMessagesBetween(BaseUser user1, BaseUser user2) {
        return messages.stream()
                .filter(m -> (m.getSender().equals(user1) && m.getReceiver().equals(user2)) ||
                             (m.getSender().equals(user2) && m.getReceiver().equals(user1)))
                .collect(Collectors.toList());
    }

    /**
     * Saves messages to file for persistence.
     */
    private void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads messages from file.
     */
    @SuppressWarnings("unchecked")
    private List<Message> loadMessages() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<Message>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}
