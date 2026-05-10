package com.mindshield.dao;

import com.mindshield.models.ModerationReport;
import java.util.List;

public interface ModerationDao {
    void save(ModerationReport report);
    void update(ModerationReport report);
    ModerationReport findById(String id);
    List<ModerationReport> findAll();
}
