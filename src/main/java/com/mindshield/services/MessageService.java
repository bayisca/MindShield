package com.mindshield.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.mindshield.models.BaseUser;
import com.mindshield.models.Message;

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

    
    // Sends a direct message from one user to another.
    
    public Message sendMessage(BaseUser sender, BaseUser receiver, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        Message message = new Message(sender, receiver, content);
        messages.add(message);
        saveMessages();
        return message;
    }

    //Retrieves all messages exchanged between two specific users.

    public List<Message> getMessagesBetween(BaseUser user1, BaseUser user2) {
        return messages.stream()
                .filter(m -> (m.getSender().equals(user1) && m.getReceiver().equals(user2)) ||
                             (m.getSender().equals(user2) && m.getReceiver().equals(user1)))
                .collect(Collectors.toList());
    }

    /** Returns true if at least one message exists between the two users. */
    public boolean hasChatBetween(BaseUser user1, BaseUser user2) {
        return messages.stream()
                .anyMatch(m -> (m.getSender().equals(user1) && m.getReceiver().equals(user2)) ||
                               (m.getSender().equals(user2) && m.getReceiver().equals(user1)));
    }

    /** Hesap kapatılırken ilgili tüm DM kayıtlarını kaldırır. */
    public void purgeInvolvingPersona(String persona) {
        if (persona == null || persona.isBlank()) {
            return;
        }
        messages.removeIf(m -> {
            if (m == null) {
                return false;
            }
            boolean from = m.getSender() != null && persona.equals(m.getSender().getPersona());
            boolean to = m.getReceiver() != null && persona.equals(m.getReceiver().getPersona());
            return from || to;
        });
        saveMessages();
    }

    //Saves messages to file for persistence.

    private void saveMessages() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Loads messages from file.
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
