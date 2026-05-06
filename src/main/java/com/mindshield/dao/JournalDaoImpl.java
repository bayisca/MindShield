package com.mindshield.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.mindshield.models.JournalEntry;

public class JournalDaoImpl implements JournalDao {
    private List<JournalEntry> entries;
    private static final String DATA_FILE = "journal_entries.dat";

    public JournalDaoImpl() {
        this.entries = load();
    }

    @Override
    public void save(JournalEntry entry) {
        entries.add(entry);
        persist();
    }

    @Override
    public void updateAll() {
        persist();
    }

    @Override
    public void deleteById(String id) {
        if (id == null) return;
        boolean removed = entries.removeIf(e -> e != null && id.equals(e.getId()));
        if (removed) {
            persist();
        }
    }

    @Override
    public JournalEntry findById(String id) {
        if (id == null) return null;
        return entries.stream()
                .filter(e -> id.equals(e.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<JournalEntry> findAll() {
        return new ArrayList<>(entries);
    }

    @Override
    public void deleteEntriesByAuthorPersona(String persona) {
        if (persona == null || persona.isBlank()) {
            return;
        }
        boolean removed = entries.removeIf(e ->
                e != null && e.getAuthor() != null && persona.equals(e.getAuthor().getPersona()));
        if (removed) {
            persist();
        }
    }

    private void persist() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(entries);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private List<JournalEntry> load() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<JournalEntry>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}
