package com.mindshield.dao;

import com.mindshield.models.ChatRoom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomDaoImpl implements ChatRoomDao {
    private List<ChatRoom> dbMock;
    private static final String DATA_FILE = "chatrooms.dat";

    public ChatRoomDaoImpl() {
        this.dbMock = load();
    }

    @Override
    public void save(ChatRoom room) {
        dbMock.add(room);
        persist();
    }

    @Override
    public void update(ChatRoom room) {
        deleteById(room.getId());
        save(room);
    }

    @Override
    public void deleteById(String id) {
        if (id == null) return;
        boolean removed = dbMock.removeIf(r -> r != null && id.equals(r.getId()));
        if (removed) {
            persist();
        }
    }

    @Override
    public ChatRoom findById(String id) {
        if (id == null) return null;
        return dbMock.stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ChatRoom> findAll() {
        return new ArrayList<>(dbMock);
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(dbMock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<ChatRoom> load() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<ChatRoom>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}