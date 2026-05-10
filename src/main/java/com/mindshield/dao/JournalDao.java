package com.mindshield.dao;

import java.util.List;

import com.mindshield.models.JournalEntry;

public interface JournalDao {
    void save(JournalEntry entry);

    void update(JournalEntry entry);

    void deleteById(String id);

    JournalEntry findById(String id);

    List<JournalEntry> findAll();

    /** Yazar ID'sine göre tüm günlük kayıtlarını siler. */
    void deleteEntriesByUserId(String userId);
}
