package com.mindshield.dao;

import com.mindshield.models.BaseUser;
import java.util.List;

public interface UserDao {
    void save(BaseUser user);
    BaseUser findByPersona(String persona);
    boolean existsByPersona(String persona);
    List<BaseUser> findAll();
    void deleteByPersona(String persona);
}
